package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;

public class Permission extends Command {

    public Permission() {
        super(new String[]{"permission"}, "", "%s grant <Permission> <Mentioned Users>\n %s deny <Permission> <Mentioned Users>");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(full.equalsIgnoreCase(args[0])){
            event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
        }else{
            if(isCommand(args, "grant")){
                if(args.length >= 3){
                    String permission = args[1].toLowerCase();
                    for(Member member: event.getMessage().getMentionedMembers()){
                        ArrayList<String> permissions = ozzie.getConfigurationManager().getUserPermissions(event.getGuild().getIdLong(), member.getIdLong());
                        if(permissions.isEmpty()){
                            permissions.add(permission);
                            event.getChannel().sendMessage(String.format("%s has a their first permission \"%s\"", member.getAsMention(), permission.toLowerCase())).queue();
                        }else{
                            if(permissions.contains(permission)){
                                event.getChannel().sendMessage(String.format("%s already has a the permission \"%s\"", member.getAsMention(), permission.toLowerCase())).queue();
                            }else{
                                permissions.add(permission);
                                event.getChannel().sendMessage(String.format("%s has a new permission \"%s\"", member.getAsMention(), permission.toLowerCase())).queue();
                            }
                        }
                        ozzie.getConfigurationManager().updateUserPermissions(event.getGuild().getIdLong(), member.getIdLong(), permissions);
                    }
                }else{
                    event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
                }
            }else if(isCommand(args, "deny")){
                if(args.length >= 3){
                    String permission = args[1].toLowerCase();
                    for(Member member: event.getMessage().getMentionedMembers()){
                        ArrayList<String> permissions = ozzie.getConfigurationManager().getUserPermissions(event.getGuild().getIdLong(), member.getIdLong());
                        if(permissions.isEmpty()){
                            event.getChannel().sendMessage(String.format("%s does not have any permissions", member.getAsMention())).queue();
                        }else{
                            if(permissions.contains(permission)){
                                permissions.remove(permission);
                                event.getChannel().sendMessage(String.format("%s no longer has the permission \"%s\"", member.getAsMention(), permission.toLowerCase())).queue();
                            }else{
                                event.getChannel().sendMessage(String.format("%s does not have the permission \"%s\"", member.getAsMention(), permission.toLowerCase())).queue();
                            }
                        }
                        ozzie.getConfigurationManager().updateUserPermissions(event.getGuild().getIdLong(), member.getIdLong(), permissions);
                    }
                }else{
                    event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
                }
            }
        }
    }
}
