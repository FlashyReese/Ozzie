package me.flashyreese.ozzie.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.ozzie.api.command.Command;
import me.flashyreese.ozzie.api.command.CommandManager;
import me.flashyreese.ozzie.api.command.argument.RoleArgumentType;
import me.flashyreese.ozzie.api.command.argument.TextChannelArgumentType;
import me.flashyreese.ozzie.api.command.argument.UserArgumentType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AboutCommand extends Command {
    public AboutCommand(String category, String description, String important) {
        super(category, description, important, "ozzie.about");
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getArgumentBuilder() {
        return CommandManager.literal("hello").requires(this::hasPermission).executes(commandContext -> {
            commandContext.getSource().getChannel().sendMessage("Hello").queue();
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }).then(CommandManager.argument("textchannel", TextChannelArgumentType.textChannel()).executes(commandContext -> {
            commandContext.getSource().getChannel().sendMessage(TextChannelArgumentType.getTextChannel(commandContext, "textchannel").getAsMention()).queue();
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }).then(CommandManager.argument("user", UserArgumentType.user()).executes(commandContext -> {
            commandContext.getSource().getChannel().sendMessage(UserArgumentType.getUser(commandContext, "user").getAsMention()).queue();
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }).then(CommandManager.argument("role", RoleArgumentType.role()).executes(commandContext -> {
            commandContext.getSource().getChannel().sendMessage(RoleArgumentType.getRole(commandContext, "role").getAsMention()).queue();
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }))));
    }
}
