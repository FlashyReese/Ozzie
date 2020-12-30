package me.flashyreese.ozzie.api.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import net.dv8tion.jda.api.entities.Role;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class RoleArgumentType implements ArgumentType<Long> {

    public static RoleArgumentType role() {
        return new RoleArgumentType();
    }

    public static Role getRole(CommandContext<DiscordCommandSource> context, String name) throws CommandSyntaxException {
        long id = context.getArgument(name, Long.class);
        return context.getSource()
                .getEvent()
                .getMessage()
                .getMentionedRoles()
                .stream()
                .filter(channel -> channel.getIdLong() == id)
                .findFirst()
                .orElse(null);
    }


    @Override
    public Long parse(StringReader stringReader) throws CommandSyntaxException {
        long roleId = 0L;
        if (!stringReader.canRead()) {
            return 0L;
        } else {
            if (stringReader.peek() == '<') {
                roleId = getRoleId(stringReader, roleId);
            } else if (stringReader.peek() == '\\') {
                if (stringReader.peek() == '<') {
                    roleId = getRoleId(stringReader, roleId);
                }
            }
        }
        return roleId;
    }

    private long getRoleId(StringReader stringReader, long roleId) throws CommandSyntaxException {
        stringReader.skip();
        if (stringReader.peek() == '@') {
            stringReader.skip();
            if (stringReader.peek() == '&') {
                stringReader.skip();
                roleId = stringReader.readLong();
                if (stringReader.peek() == '>') {
                    stringReader.skip();
                }
            }
        }
        return roleId;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return null;
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.asList("<@&1234567890>", "\\<@&1234567890>");
    }
}
