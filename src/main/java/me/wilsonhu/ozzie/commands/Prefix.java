package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.i18n.ParsableText;
import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import me.wilsonhu.ozzie.schemas.UserSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Prefix extends Command {
    public Prefix() {
        super(new String[]{"prefix"}, "description", "syntax");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(full.equalsIgnoreCase(args[0])){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            if(serverSchema.isAllowUserCustomCommandPrefix()){
                UserSchema userSchema = ozzie.getConfigurationManager().getUserSettings(event.getAuthor().getIdLong());
                if(!userSchema.getUserLocale().equals(ozzie.getDefaultCommandPrefix())){
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentprefixuser", event), event.getAuthor().getName(), ozzie.getI18nManager().getLocaleDisplayName(userSchema.getUserLocale())).toString()).queue();
                }else{
                    defaultServerPrefixCheck(event, ozzie, serverSchema);
                }
            }else{
                defaultServerPrefixCheck(event, ozzie, serverSchema);
            }
        }else if(isCommand(args, "set", "allowuser")){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            boolean prefix = Boolean.parseBoolean(args[2]);
            if(serverSchema.isAllowUserCustomCommandPrefix() == prefix){
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.servercustomprefixalready", event), (serverSchema.isAllowUserCustomCommandPrefix() ? new TranslatableText("ozzie.true").toString() : new TranslatableText("ozzie.false", event).toString())).toString()).queue();
            }else{
                serverSchema.setAllowUserCustomCommandPrefix(prefix);
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.servercustomprefix", event), (serverSchema.isAllowUserCustomCommandPrefix() ? new TranslatableText("ozzie.true").toString() : new TranslatableText("ozzie.false", event).toString())).toString()).queue();
                ozzie.getConfigurationManager().updateServerSettings(event.getGuild().getIdLong(), serverSchema);
            }
        }else if(isCommand(args, "set", "server")){//Fixme: Seperate into a different class
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            String prefix = args[2];
            if(serverSchema.getCustomCommandPrefix().equalsIgnoreCase(prefix)){
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.serversetprefixalready", event), serverSchema.getServerLocale()).toString()).queue();
            }else{
                serverSchema.setCustomCommandPrefix(prefix);
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.serversetprefix", event), serverSchema.getServerLocale()).toString()).queue();
                ozzie.getConfigurationManager().updateServerSettings(event.getGuild().getIdLong(), serverSchema);
            }
        }else if(isCommand(args, "set")){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            if(serverSchema.isAllowUserCustomCommandPrefix()){
                UserSchema userSchema = ozzie.getConfigurationManager().getUserSettings(event.getAuthor().getIdLong());
                String prefix = args[1];
                if(userSchema.getCustomCommandPrefix().equalsIgnoreCase(prefix)){
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.usersetcustomprefixalready", event), event.getAuthor().getName(), userSchema.getCustomCommandPrefix()).toString()).queue();
                }else{
                    userSchema.setCustomCommandPrefix(prefix);
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.usersetcustomprefix", event), event.getAuthor().getName(), userSchema.getCustomCommandPrefix()).toString()).queue();
                    ozzie.getConfigurationManager().updateUserSettings(event.getAuthor().getIdLong(), userSchema);
                }
            }else{
                event.getChannel().sendMessage(new TranslatableText("ozzie.servercustomprefixdisabled", event).toString()).queue();
            }
        }
    }

    private void defaultServerPrefixCheck(MessageReceivedEvent event, Ozzie ozzie, ServerSchema serverSchema) {
        if(!serverSchema.getCustomCommandPrefix().equals(ozzie.getDefaultCommandPrefix())){
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentprefixserver", event), serverSchema.getCustomCommandPrefix()).toString()).queue();
        }else{
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentprefix", event), serverSchema.getCustomCommandPrefix()).toString()).queue();
        }
    }
}
