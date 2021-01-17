/*
 * Copyright © 2021 FlashyReese <reeszrbteam@gmail.com>
 *
 * This file is part of Ozzie.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.ozzie.api.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import net.dv8tion.jda.api.entities.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Represents User Argument Type.
 *
 * @author FlashyReese
 * @version 0.9.0+build-20210105
 * @since 0.9.0+build-20210105
 */
public class UserArgumentType implements ArgumentType<Long> {

    /**
     * Create a new UserArgumentType.
     *
     * @return UserArgumentType
     */
    public static UserArgumentType user() {
        return new UserArgumentType();
    }

    /**
     * Retrieve User from Command Context.
     *
     * @param context DiscordCommandSource Command Context
     * @param name Name of the argument
     * @return User
     */
    public static User getUser(CommandContext<DiscordCommandSource> context, String name) throws CommandSyntaxException {
        long id = context.getArgument(name, Long.class);
        return context.getSource()
                .getEvent()
                .getMessage()
                .getMentionedUsers()
                .stream()
                .filter(channel -> channel.getIdLong() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Parses StringReader to Long.
     *
     * @param stringReader StringReader
     * @return Long
     * @throws CommandSyntaxException if input does not match format
     */
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
                stringReader.skip();
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
        return Arrays.asList("<@!1234567890>", "\\<@!1234567890>", "\\<@1234567890>");
    }
}
