package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
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
    public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(full.equals(getNames()[0])){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            if(serverSchema.isAllowUserLocale()){
                UserSchema userSchema = ozzie.getConfigurationManager().getUserSettings(event.getAuthor().getIdLong());
                if(!userSchema.getUserLocale().equals("default")){
                    event.getChannel().sendMessage(new TranslatableText("ozzie.currentlocaleuser", event).toString() + " " + ozzie.getI18nManager().getLocaleDisplayName(userSchema.getUserLocale())).queue();
                }else{
                    defaultServerLocaleCheck(event, ozzie, serverSchema);
                }
            }else{
                defaultServerLocaleCheck(event, ozzie, serverSchema);
            }
        }
    }

    private void defaultServerLocaleCheck(MessageReceivedEvent event, Ozzie ozzie, ServerSchema serverSchema) {
        if(!serverSchema.getServerLocale().equals("default")){
            event.getChannel().sendMessage(new TranslatableText("ozzie.currentlocaleserver", event).toString() + " " + ozzie.getI18nManager().getLocaleDisplayName(serverSchema.getServerLocale())).queue();
        }else{
            event.getChannel().sendMessage(new TranslatableText("ozzie.currentlocale", event).toString() + " " + ozzie.getI18nManager().getLocaleDisplayName(Locale.getDefault().toString())).queue();
        }
    }
}
