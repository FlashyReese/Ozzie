/*
 * Copyright © 2021 FlashyReese <reeszrbteam@gmail.com>
 *
 * This file is part of Ozzie.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.ozzie.api.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

/**
 * Represents Command Interface
 *
 * @author FlashyReese
 * @version 0.9.0+build-20210105
 * @since 0.9.0+build-20210105
 */
public interface Command<S> {
    LiteralArgumentBuilder<S> getArgumentBuilder();

    default LiteralArgumentBuilder<S> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    default <T> RequiredArgumentBuilder<S, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}
