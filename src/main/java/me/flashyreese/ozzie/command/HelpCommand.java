package me.flashyreese.ozzie.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.common.permission.PermissionException;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.Command;
import me.flashyreese.ozzie.api.command.CommandManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Map;
import java.util.Optional;

public class HelpCommand extends Command {
    public HelpCommand(String category, String description, String important) {
        super(category, description, important, "ozzie.help");
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getArgumentBuilder() {
        return CommandManager.literal("help").requires(this::hasPermission).executes(commandContext -> {
            MessageReceivedEvent event = commandContext.getSource();
            Map<String, Boolean> permissions = CommandManager.permissionMap(event);

            EmbedBuilder embed = new EmbedBuilder().setColor(Color.orange).setTitle(OzzieApi.INSTANCE.getBotName());
            int adder = 0;
            for (String cc : OzzieApi.INSTANCE.getCommandManager().getCategories()) {
                String name = cc.isEmpty() ? "Uncategorized" : cc.substring(0, 1).toUpperCase() + cc.substring(1).toLowerCase();
                StringBuilder line = new StringBuilder();
                for (CommandManager.CommandContainer commandContainer : OzzieApi.INSTANCE.getCommandManager().getCommandContainers()) {
                    Command cmd = commandContainer.getCommand();
                    try {
                        if (!cmd.isHidden() && cmd.getCategory().equalsIgnoreCase(cc) && OzzieApi.INSTANCE.getPermissionDispatcher().hasPermission(cmd.getPermission(), permissions)) {
                            line.append(String.format("`%s` ", cmd.getArgumentBuilder().getLiteral()));

                            adder++;
                        }
                    } catch (PermissionException e) {
                        e.printStackTrace();
                    }
                }
                if (!line.toString().isEmpty()) {
                    embed.addField(name, line.toString(), false);
                }
            }
            embed.setFooter(String.format("Total Commands: %s", adder), null);
            event.getChannel().sendMessage(embed.build()).queue();
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }).then(CommandManager.argument("commandName", StringArgumentType.word()).executes(commandContext -> {
            Optional<CommandManager.CommandContainer> optional = OzzieApi.INSTANCE.getCommandManager().getCommandContainers().stream().filter(command -> command.getCommand().getArgumentBuilder().getLiteral().equalsIgnoreCase(StringArgumentType.getString(commandContext, "commandName"))).findFirst();
            optional.ifPresent(commandContainer -> commandContext.getSource().getChannel().sendMessage(commandContainer.getCommand().getArgumentBuilder().build().getChildren().toString()).queue());
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }));
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}

