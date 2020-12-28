package me.flashyreese.ozzie.api.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class TextChannelArgumentType implements ArgumentType<Long> {

    public static TextChannelArgumentType textChannel() {
        return new TextChannelArgumentType();
    }

    public static TextChannel getTextChannel(CommandContext<MessageReceivedEvent> context, String name) throws CommandSyntaxException {
        long id = context.getArgument(name, Long.class);
        return context.getSource()
                .getMessage()
                .getMentionedChannels()
                .stream()
                .filter(channel -> channel.getIdLong() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long parse(StringReader stringReader) throws CommandSyntaxException {
        long textChannelId = 0L;
        if (!stringReader.canRead()) {
            return 0L;
        } else {
            if (stringReader.peek() == '\\') {
                stringReader.skip();
            }
            textChannelId = getTextChannelId(stringReader, textChannelId);
        }
        return textChannelId;
    }

    private long getTextChannelId(StringReader stringReader, long textChannelId) throws CommandSyntaxException {
        if (stringReader.peek() == '<') {
            stringReader.skip();
            if (stringReader.peek() == '#') {
                stringReader.skip();
                textChannelId = stringReader.readLong();
                if (stringReader.peek() == '>') {
                    stringReader.skip();
                }
            }
        }
        return textChannelId;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return null;
    }

    @Override
    public Collection<String> getExamples() {
        return null;
    }
}
