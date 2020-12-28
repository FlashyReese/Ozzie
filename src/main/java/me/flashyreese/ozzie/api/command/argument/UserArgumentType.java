package me.flashyreese.ozzie.api.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class UserArgumentType implements ArgumentType<Long> {

    public static UserArgumentType user() {
        return new UserArgumentType();
    }

    public static User getUser(CommandContext<MessageReceivedEvent> context, String name) throws CommandSyntaxException {
        long id = context.getArgument(name, Long.class);
        return context.getSource()
                .getMessage()
                .getMentionedUsers()
                .stream()
                .filter(channel -> channel.getIdLong() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long parse(StringReader stringReader) throws CommandSyntaxException {
        long userId = 0L;
        if (!stringReader.canRead()) {
            return 0L;
        } else {
            if (stringReader.peek() == '<') {
                stringReader.skip();
                if (stringReader.peek() == '@') {
                    stringReader.skip();
                    if (stringReader.peek() == '!') {
                        stringReader.skip();
                        userId = stringReader.readLong();
                        if (stringReader.peek() == '>') {
                            stringReader.skip();
                        }
                    }
                }
            } else if (stringReader.peek() == '\\') {
                if (stringReader.peek() == '<') {
                    stringReader.skip();
                    if (stringReader.peek() == '@') {
                        stringReader.skip();
                        if (stringReader.peek() == '!') {
                            stringReader.skip();
                        }
                        userId = stringReader.readLong();
                        if (stringReader.peek() == '>') {
                            stringReader.skip();
                        }
                    }
                }
            }
        }
        return userId;
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
