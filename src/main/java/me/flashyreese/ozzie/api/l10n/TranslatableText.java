package me.flashyreese.ozzie.api.l10n;

import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class TranslatableText implements CharSequence{
    private final String key;
    private String lang;

    public TranslatableText(String key, String lang) {
        this.key = key;
        this.lang = lang;
    }

    public TranslatableText(String key, CommandContext<DiscordCommandSource> commandContext){
        DiscordCommandSource commandSource = commandContext.getSource();
        this.key = key;
        ServerConfigurationSchema serverConfigurationSchema = commandSource.getServerConfigurationSchema();
        this.lang = serverConfigurationSchema.getLocale().toLowerCase();

        if (this.lang.isEmpty())
            this.lang = "en_us"; //Fixme: can't really fix lol

        if (serverConfigurationSchema.isAllowUserLocale()){
            if (!commandSource.getUserSchema().getLocale().isEmpty()){
                this.lang = commandSource.getUserSchema().getLocale().toLowerCase();
            }
        }
    }

    public TranslatableText(String key, MessageReceivedEvent event){
        this.key = key;
        if (event.isFromGuild()){
            try {
                ServerConfigurationSchema serverConfigurationSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveServerConfiguration(event.getGuild().getIdLong());
                UserSchema userSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveUser(event.getAuthor().getIdLong());

                this.lang = serverConfigurationSchema.getLocale().toLowerCase();

                if (this.lang.isEmpty())
                    this.lang = "en_us"; //Fixme: can't really fix lol

                if (serverConfigurationSchema.isAllowUserLocale()){
                    if (!userSchema.getLocale().isEmpty()){
                        this.lang = userSchema.getLocale().toLowerCase();
                    }
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }else{
            try {
                UserSchema userSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveUser(event.getAuthor().getIdLong());
                this.lang = "en_us";
                if (!userSchema.getLocale().isEmpty()){
                    this.lang = userSchema.getLocale().toLowerCase();
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @Override
    public int length() {
        return this.toString().length();
    }

    @Override
    public char charAt(int index) {
        return this.toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.toString().subSequence(start, end);
    }

    public @NotNull String toString() {
        return OzzieApi.INSTANCE.getL10nManager().translate(this.key, this.lang);
    }
}