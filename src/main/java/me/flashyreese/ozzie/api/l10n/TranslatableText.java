package me.flashyreese.ozzie.api.l10n;

import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import org.jetbrains.annotations.NotNull;

public class TranslatableText implements CharSequence{
    private final String key;
    private String lang;

    public TranslatableText(String key, String lang) {
        this.key = key;
        this.lang = lang;
    }

    public TranslatableText(String key, CommandContext<DiscordCommandSource> commandContext){
        this.key = key;
        ServerConfigurationSchema serverConfigurationSchema = commandContext.getSource().getServerConfigurationSchema();
        this.lang = serverConfigurationSchema.getLocale().toLowerCase();

        if (this.lang.isEmpty())
            this.lang = "en_us"; //Fixme: can't really fix lol

        if (serverConfigurationSchema.isAllowUserLocale()){
            if (!commandContext.getSource().getUserSchema().getLocale().isEmpty()){
                this.lang = commandContext.getSource().getUserSchema().getLocale().toLowerCase();
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