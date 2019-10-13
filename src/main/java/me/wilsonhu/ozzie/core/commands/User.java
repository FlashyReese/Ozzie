package me.wilsonhu.ozzie.core.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import me.wilsonhu.ozzie.manager.command.CommandCategory;
import me.wilsonhu.ozzie.manager.command.CommandLevel;
import me.wilsonhu.ozzie.manager.json.configuration.ServerSettings;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class User extends Command{

	
	public User() {
		super(new String[]{"user"}, "Turn on whitelist, add or remove users from whitelist and blacklist", "%s whitelist add (Guild ID) <Mentioned User>\n%s whitelist remove (Guild ID) <Mentioned User>\n%s blacklist add (Guild ID) <Mentioned User>\n%s blacklist remove (Guild ID) <Mentioned User>\n%s whitelist on (Guild ID)\n%s whitelist off (Guild ID)");
		this.setCategory(CommandCategory.SETTINGS);
		this.setLevel(CommandLevel.ADMINISTRATOR);
		this.setGuildOnly(true);//Probably can fix this to make it work with PMs
		this.setPermission("ozzie.developer");
	}

	@Override
	public void onCommand(String full, String argss, MessageReceivedEvent event, Ozzie ozzie){
		//re write this garbage v:
		try {
			String[] args = argss.split(" ");
			String args1 = args[0];
			String args2 = args[1];
			Guild guild = event.getGuild();
			if(args.length == 3 || args2.equalsIgnoreCase("on") || args2.equalsIgnoreCase("off")) {
				guild = ozzie.getJDA().getGuildById(args[2]);
			}
			ServerSettings ss = ozzie.getOzzieManager().getServerSettingsManager().getServerSettingsList().get(guild.getIdLong());
			if(args1.equalsIgnoreCase("whitelist")) {
				if(args2.equalsIgnoreCase("add")) {
					Member m = event.getMessage().getMentionedMembers().get(0);
					if(ss.getWhitelistedUsers().contains(m.getUser().getIdLong())) {
						event.getChannel().sendMessage(m.getAsMention() + " is already on " + guild.getName() + "'s whitelist").queue();
						return;
					}
					ss.getWhitelistedUsers().add(m.getUser().getIdLong());
					event.getChannel().sendMessage(m.getAsMention() + " has been added to " + guild.getName() + "'s whitelist").queue();
				}else if(args2.equalsIgnoreCase("remove")) {
					Member m = event.getMessage().getMentionedMembers().get(0);
					if(!ss.getWhitelistedUsers().contains(m.getUser().getIdLong())) {
						event.getChannel().sendMessage(m.getAsMention() + " is not on " + guild.getName() + "'s whitelist").queue();
						return;
					}
					ss.getWhitelistedUsers().remove(m.getUser().getIdLong());
					event.getChannel().sendMessage(m.getAsMention() + " has been removed from " + guild.getName() + "'s whitelist").queue();
				}else if(args2.equalsIgnoreCase("on")) {
					if(ss.isWhiteListMode()) {
						event.getChannel().sendMessage(guild.getName() + "'s Whitelist mode is already enabled").queue();
						return;
					}
					ss.setWhiteListMode(true);
					event.getChannel().sendMessage("Enabling " + guild.getName() + "'s whitelist mode").queue();
				}else if(args2.equalsIgnoreCase("off")) {
					if(!ss.isWhiteListMode()) {
						event.getChannel().sendMessage(guild.getName() + "'s Whitelist mode is not enabled").queue();
						return;
					}
					ss.setWhiteListMode(false);
					event.getChannel().sendMessage("Disabling  " + guild.getName() + "'s whitelist mode").queue();
				}
			}else if(args1.equalsIgnoreCase("blacklist")) {
				if(args2.equalsIgnoreCase("add")) {
					Member m = event.getMessage().getMentionedMembers().get(0);
					if(ss.getBlacklistedUsers().contains(m.getUser().getIdLong())) {
						event.getChannel().sendMessage(m.getAsMention() + " is already on " + guild.getName() + "'s blacklist").queue();
						return;
					}
					ss.getBlacklistedUsers().add(m.getUser().getIdLong());
					event.getChannel().sendMessage(m.getAsMention() + " has been added to " + guild.getName() + "'s blacklist").queue();
				}else if(args2.equalsIgnoreCase("remove")) {
					Member m = event.getMessage().getMentionedMembers().get(0);
					if(!ss.getBlacklistedUsers().contains(m.getUser().getIdLong())) {
						event.getChannel().sendMessage(m.getAsMention() + " is not on " + guild.getName() + "'s blacklist").queue();
						return;
					}
					ss.getBlacklistedUsers().remove(m.getUser().getIdLong());
					event.getChannel().sendMessage(m.getAsMention() + " has been removed from " + guild.getName() + "'s blacklist").queue();
				}
			}else if(args1.equalsIgnoreCase("permissions")){
				String args3 = args[2];
				args3 = args3.toLowerCase();
				if(args2.equalsIgnoreCase("add")) {
					Member m = event.getMessage().getMentionedMembers().get(0);
					Boolean[] bool = ozzie.getOzzieManager().getPermissionManager().addPermission(m.getIdLong(), args3.toLowerCase());
					if(bool[0] == true && bool[1] == true) {
						event.getChannel().sendMessage(String.format("%s already has a the permission \"%s\"", m.getAsMention(), args3.toLowerCase())).queue();
					}else if(bool[0] == true && bool[1] == false) {
						event.getChannel().sendMessage(String.format("%s has a new permission \"%s\"", m.getAsMention(), args3.toLowerCase())).queue();
					}else {
						event.getChannel().sendMessage(String.format("%s has a their first permission \"%s\"", m.getAsMention(), args3.toLowerCase())).queue();
					}
					ozzie.getOzzieManager().getJsonManager().writeUserPermissionList();
				}else if(args2.equalsIgnoreCase("remove")) {
					Member m = event.getMessage().getMentionedMembers().get(0);
					Boolean[] bool = ozzie.getOzzieManager().getPermissionManager().removePermission(m.getIdLong(), args3.toLowerCase());
					if(bool[0] == true && bool[1] == true) {
						event.getChannel().sendMessage(String.format("%s no longer has the permission \"%s\"", m.getAsMention(), args3.toLowerCase())).queue();
					}else if(bool[0] == true && bool[1] == false) {
						event.getChannel().sendMessage(String.format("%s does not have the permission \"%s\"", m.getAsMention(), args3.toLowerCase())).queue();
					}else {
						event.getChannel().sendMessage(String.format("%s does not have any permissions", m.getAsMention())).queue();
					}
					ozzie.getOzzieManager().getJsonManager().writeUserPermissionList();
				}
			}
			ozzie.getOzzieManager().getServerSettingsManager().getServerSettingsList().replace(guild.getIdLong(), ss);
			ozzie.getOzzieManager().getJsonManager().writeServerSettingsList();
		}catch(Exception e) {
			e.printStackTrace();
			event.getChannel().sendMessage(this.getHelpEmblem()).queue();
		}
	}

}
