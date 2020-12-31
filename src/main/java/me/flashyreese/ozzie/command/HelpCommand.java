package me.flashyreese.ozzie.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.flashyreese.common.permission.PermissionException;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.CommandManager;
import me.flashyreese.ozzie.api.l10n.ParsableText;
import me.flashyreese.ozzie.api.l10n.TranslatableText;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HelpCommand extends DiscordCommand {
    public HelpCommand() {
        super("", "ozzie.help.description", "ozzie.help");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("help")
                .requires(this::hasPermission)
                .executes(this::displayCommands)
                .then(DiscordCommandManager.argument("commandName", StringArgumentType.word())
                        .executes(this::getHelpForCommand));
    }

    private int displayCommands(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        Map<String, Boolean> permissions = commandContext.getSource().permissions();

        EmbedBuilder embed = new EmbedBuilder().setColor(Color.orange).setTitle(OzzieApi.INSTANCE.getBotName());
        int adder = 0;
        for (String cc : OzzieApi.INSTANCE.getCommandManager().getCategories()) {
            String name = cc.isEmpty() ? new TranslatableText("ozzie.help.uncategorized", commandContext).toString() : new TranslatableText(cc, commandContext).toString();
            StringBuilder line = new StringBuilder();
            for (CommandManager.CommandContainer<DiscordCommandSource, DiscordCommand> commandContainer : OzzieApi.INSTANCE.getCommandManager().getCommandContainers()) {
                DiscordCommand cmd = commandContainer.getCommand();
                try {
                    if (!cmd.isHidden() && cmd.getCategory().equalsIgnoreCase(cc) && OzzieApi.INSTANCE.getPermissionDispatcher()
                            .hasPermission(cmd
                                    .getPermission(), permissions)) {
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
        embed.setFooter(new ParsableText(new TranslatableText("ozzie.help.total_commands", commandContext), String.valueOf(adder)).toString(), null);
        event.getChannel().sendMessage(embed.build()).queue();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    //Fixme: works for the time being, Brigadier's smart usage is limited.
    private int getHelpForCommand(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        CommandDispatcher<DiscordCommandSource> dispatcher = OzzieApi.INSTANCE.getCommandManager().getDispatcher();

        String commandName = StringArgumentType.getString(commandContext, "commandName");
        Optional<CommandManager.CommandContainer<DiscordCommandSource, DiscordCommand>> optionalCommandContainer = OzzieApi.INSTANCE.getCommandManager().getCommandContainers().stream().filter(container -> container.getIdentifier().getName().equalsIgnoreCase(commandName)).findFirst();
        if (optionalCommandContainer.isPresent()) {
            CommandManager.CommandContainer<DiscordCommandSource, DiscordCommand> commandContainer = optionalCommandContainer.get();
            CommandNode<DiscordCommandSource> commandNode = commandContainer.getCommand().getArgumentBuilder().build();

            StringBuilder syntaxTest = new StringBuilder();
            for (CommandExampleContainer commandExampleContainer : this.getCommandExampleContainers(commandNode, commandContext, dispatcher, new ArrayList<>())) {
                if (dispatcher.parse(commandExampleContainer.getValue(), commandContext.getSource()).getContext().getCommand() != null) {
                    syntaxTest.append(commandExampleContainer.getKey()).append("\n");
                }
            }

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(new ParsableText(new TranslatableText("ozzie.help.for_command", commandContext), commandNode.getName()).toString(), null, event.getJDA().getSelfUser().getAvatarUrl())
                    .setDescription(new TranslatableText(commandContainer.getCommand().getDescription(), commandContext))
                    .addField(new TranslatableText("ozzie.help.syntax", commandContext).toString(), syntaxTest.toString(), true)
                    .addField(new TranslatableText("ozzie.help.permission", commandContext).toString(), commandContainer.getCommand().getPermission(), true)
                    .setFooter(new ParsableText(new TranslatableText("ozzie.help.request_by", commandContext), event.getAuthor().getName()).toString(), event.getAuthor().getAvatarUrl())
                    .build();


            event.getChannel().sendMessage(embed).queue();
        } else {
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.help.invalid_command", commandContext), commandName)).queue();
        }

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private List<CommandExampleContainer> getCommandExampleContainers(CommandNode<DiscordCommandSource> commandNode, CommandContext<DiscordCommandSource> commandContext, CommandDispatcher<DiscordCommandSource> dispatcher, List<CommandExampleContainer> list) {
        if (list.isEmpty()) {
            list.add(new CommandExampleContainer(commandNode.getName(), commandNode.getName()));
        }
        CommandExampleContainer previousStringFromList = list.get(list.size() - 1);
        for (CommandNode<DiscordCommandSource> child : commandNode.getChildren()) {
            boolean allow = child.getRequirement().test(commandContext.getSource());
            if (!allow)
                continue;

            String newStringKey = previousStringFromList.getKey();
            String newStringValue = previousStringFromList.getValue();

            //Check if command can be ran or not and if literal, argument or both but optional
            if (child instanceof LiteralCommandNode) {
                newStringKey += " " + child.getName();
                newStringValue += " " + child.getName();
            } else {
                if (child.getCommand() != null) {
                    if (child.getChildren().isEmpty()) {
                        newStringKey += " <" + child.getName() + ">";
                    } else {
                        newStringKey += " [" + child.getName() + "]";
                    }
                } else {
                    newStringKey += " <" + child.getName() + ">";
                }

                newStringValue += " " + child.getExamples().stream().findFirst().get();
            }

            list.add(new CommandExampleContainer(newStringKey, newStringValue));
            if (!child.getChildren().isEmpty()) {
                list = this.getCommandExampleContainers(child, commandContext, dispatcher, list);
            }
        }
        return list;
    }

    private static class CommandExampleContainer {
        private final String key;
        private final String value;

        public CommandExampleContainer(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}

