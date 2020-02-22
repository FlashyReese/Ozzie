package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.i18n.ParsableText;
import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import me.wilsonhu.ozzie.schemas.UserSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Locale;

public class Language extends Command {

    public Language() {
        super(new String[]{"language"}, "", "%s");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(full.equalsIgnoreCase(args[0])){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            if(serverSchema.isAllowUserLocale()){
                UserSchema userSchema = ozzie.getConfigurationManager().getUserSettings(event.getAuthor().getIdLong());
                if(!userSchema.getUserLocale().equals("default")){
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentlocaleuser", event), event.getAuthor().getName(), ozzie.getI18nManager().getLocaleDisplayName(userSchema.getUserLocale())).toString()).queue();
                }else{
                    defaultServerLocaleCheck(event, ozzie, serverSchema);
                }
            }else{
                defaultServerLocaleCheck(event, ozzie, serverSchema);
            }
        }else if(isCommand(args, "set", "allowuser")){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            boolean prefix = Boolean.parseBoolean(args[2]);
            if(serverSchema.isAllowUserLocale() == prefix){
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.servercustomlocalealready", event), (serverSchema.isAllowUserLocale() ? new TranslatableText("ozzie.true").toString() : new TranslatableText("ozzie.false", event).toString())).toString()).queue();
            }else{
                serverSchema.setAllowUserLocale(prefix);
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.servercustomlocale", event), (serverSchema.isAllowUserLocale() ? new TranslatableText("ozzie.true").toString() : new TranslatableText("ozzie.false", event).toString())).toString()).queue();
                ozzie.getConfigurationManager().updateServerSettings(event.getGuild().getIdLong(), serverSchema);
            }
        }else if(isCommand(args, "set", "server") && (event.getAuthor().getIdLong() == event.getGuild().getOwnerIdLong() || ozzie.getConfigurationManager().hasPermission(event.getGuild().getIdLong(), event.getAuthor().getIdLong(), "ozzie.developer"))){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            String locale = args[2];
            if(serverSchema.getServerLocale().equalsIgnoreCase(locale)){
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.serversetlocalealready", event), ozzie.getI18nManager().getLocaleDisplayName(serverSchema.getServerLocale())).toString()).queue();
            }else{
                serverSchema.setServerLocale(locale);
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.serversetlocale", event), ozzie.getI18nManager().getLocaleDisplayName(serverSchema.getServerLocale())).toString()).queue();
                ozzie.getConfigurationManager().updateServerSettings(event.getGuild().getIdLong(), serverSchema);
            }
        }else if(isCommand(args, "set")){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            if(serverSchema.isAllowUserLocale()){
                UserSchema userSchema = ozzie.getConfigurationManager().getUserSettings(event.getAuthor().getIdLong());
                String locale = args[1];
                if(userSchema.getUserLocale().equalsIgnoreCase(locale)){
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.usersetlocalealready", event), event.getAuthor().getName(), ozzie.getI18nManager().getLocaleDisplayName(userSchema.getUserLocale())).toString()).queue();
                }else{
                    userSchema.setUserLocale(locale);
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.usersetlocale", event), event.getAuthor().getName(), ozzie.getI18nManager().getLocaleDisplayName(userSchema.getUserLocale())).toString()).queue();
                    ozzie.getConfigurationManager().updateUserSettings(event.getAuthor().getIdLong(), userSchema);
                }
            }else{
                event.getChannel().sendMessage(new TranslatableText("ozzie.serverlocaledisabled", event).toString()).queue();
            }
        }
    }

    private void defaultServerLocaleCheck(MessageReceivedEvent event, Ozzie ozzie, ServerSchema serverSchema) {
        if(!serverSchema.getServerLocale().equals("default")){
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentlocaleserver", event), ozzie.getI18nManager().getLocaleDisplayName(serverSchema.getServerLocale())).toString()).queue();
        }else{
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentlocale", event), ozzie.getI18nManager().getLocaleDisplayName(Locale.getDefault().toString())).toString()).queue();
        }
    }
}
