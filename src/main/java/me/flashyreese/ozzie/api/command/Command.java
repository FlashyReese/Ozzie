package me.flashyreese.ozzie.api.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public interface Command<S> {
    LiteralArgumentBuilder<S> getArgumentBuilder();
}
