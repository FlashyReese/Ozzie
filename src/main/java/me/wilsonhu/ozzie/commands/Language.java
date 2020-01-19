package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import me.wilsonhu.ozzie.schemas.UserSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.PrintWriter;
import java.util.Locale;

public class Language extends Command {

    public Language() {
        super(new String[]{"language"}, "", "%s");
    }

    @Override
    public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(full.equals(split)){
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
        }else if(split.startsWith("server")){
            if(split.substring("server".length()).contains(" ")){
                String cmd = split.trim();
                if(cmd.contains(" ")){
                    String[] args = cmd.split(" ");
                    if(args[0].equalsIgnoreCase("set")){
                        if(args[1].equalsIgnoreCase("allowuser")){
                            if(args[2].equalsIgnoreCase("customprefix")){
                                ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
                                boolean prefix = Boolean.parseBoolean(args[3]);
                                if(serverSchema.isAllowUserCustomCommandPrefix() == prefix){
                                    event.getChannel().sendMessage(new TranslatableText("ozzie.servercustomprefix", event).toString() + " " + (serverSchema.isAllowUserCustomCommandPrefix() ? new TranslatableText("ozzie.true").toString() : new TranslatableText("ozzie.false", event).toString())).queue();
                                }else{
                                    serverSchema.setAllowUserCustomCommandPrefix(prefix);
                                    //set to new
                                    ozzie.getConfigurationManager().updateServerSettings(event.getGuild().getIdLong(), serverSchema);
                                }
                            }else if(args[2].equalsIgnoreCase("customlocale")){
                                ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
                                boolean prefix = Boolean.parseBoolean(args[3]);
                                if(serverSchema.isAllowUserLocale() == prefix){
                                    event.getChannel().sendMessage(new TranslatableText("ozzie.servercustomlocale", event).toString() + " " + (serverSchema.isAllowUserLocale() ? new TranslatableText("ozzie.true").toString() : new TranslatableText("ozzie.false", event).toString())).queue();
                                }else{
                                    serverSchema.setAllowUserLocale(prefix);
                                    //set to new
                                    ozzie.getConfigurationManager().updateServerSettings(event.getGuild().getIdLong(), serverSchema);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onCommand(String full, String split, PrintWriter writer, Ozzie ozzie) throws Exception {

    }

    private void defaultServerLocaleCheck(MessageReceivedEvent event, Ozzie ozzie, ServerSchema serverSchema) {
        if(!serverSchema.getServerLocale().equals("default")){
            event.getChannel().sendMessage(new TranslatableText("ozzie.currentlocaleserver", event).toString() + " " + ozzie.getI18nManager().getLocaleDisplayName(serverSchema.getServerLocale())).queue();
        }else{
            event.getChannel().sendMessage(new TranslatableText("ozzie.currentlocale", event).toString() + " " + ozzie.getI18nManager().getLocaleDisplayName(Locale.getDefault().toString())).queue();
        }
    }
}
