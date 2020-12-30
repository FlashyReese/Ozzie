/*
 * Copyright (C) 2019-2020 Yao Chung Hu / FlashyReese
 *
 * Ozzie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Ozzie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ozzie.  If not, see http://www.gnu.org/licenses/
 *
 */
package me.flashyreese.ozzie.api.util;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Menu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ConfirmationMenu extends Menu {

    private final Color color;
    private final String text;
    private final String description;
    private final Consumer<Message> confirm;
    private final Consumer<Message> cancel;
    private final boolean allowTypedInput;
    //private final boolean useCustomText;//useCustomEmbed;
    private final boolean useCustomEmbed;
    private final MessageEmbed customEmbed;

    public final static String CONTINUE = "\u2714";
    public final static String CANCEL = "\u274C";

    ConfirmationMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                     Color color, String text, String description, Consumer<Message> confirm,
                     Consumer<Message> cancel, boolean allowTypedInput, boolean useCustomEmbed, MessageEmbed customEmbed) {
        super(waiter, users, roles, timeout, unit);
        this.color = color;
        this.text = text;
        this.description = description;
        this.confirm = confirm;
        this.cancel = cancel;
        this.allowTypedInput = allowTypedInput;
        this.useCustomEmbed = useCustomEmbed;
        this.customEmbed = customEmbed;
    }

    /**
     * Shows the ConfirmationMenu as a new {@link net.dv8tion.jda.api.entities.Message Message}
     * in the provided {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     *
     * @param channel The MessageChannel to send the new Message to
     * @throws java.lang.IllegalArgumentException If <b>all</b> of the following are violated simultaneously:
     *                                            <ul>
     *                                                <li>Being sent to a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *                                                <li>This OrderedMenu does not allow typed input.</li>
     *                                                <li>The bot doesn't have {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION
     *                                                Permission.MESSAGE_ADD_REACTION} in the channel this menu is being sent to.</li>
     *                                            </ul>
     */
    @Override
    public void display(MessageChannel channel) {
        if (channel.getType() == ChannelType.TEXT
                && !allowTypedInput
                && !((TextChannel) channel).getGuild()
                .getSelfMember()
                .hasPermission((TextChannel) channel, Permission.MESSAGE_ADD_REACTION))
            throw new PermissionException("Must be able to add reactions if not allowing typed input!");
        initialize(channel.sendMessage(getMessage()));
    }

    @Override
    public void display(Message message) {
        if (message.getChannelType() == ChannelType.TEXT
                && !allowTypedInput
                && !message.getGuild()
                .getSelfMember()
                .hasPermission(message.getTextChannel(), Permission.MESSAGE_ADD_REACTION))
            throw new PermissionException("Must be able to add reactions if not allowing typed input!");
        initialize(message.editMessage(getMessage()));
    }

    private void initialize(RestAction<Message> ra) {
        ra.queue(m -> {
            try {
                RestAction<Void> re = m.addReaction(CONTINUE);
                re.queue();
                re = m.addReaction(CANCEL);
                re.queue(v -> {
                    // Depending on whether we are allowing text input,
                    // we call a different method.
                    if (allowTypedInput)
                        waitGeneric(m);
                    else
                        waitReactionOnly(m);
                });
            } catch (PermissionException ex) {
                // If there is a permission exception mid process, we'll still
                // attempt to make due with what we have.
                if (allowTypedInput)
                    waitGeneric(m);
                else
                    waitReactionOnly(m);
            }
        });
    }

    private void waitGeneric(Message m) {
        // Wait for a GenericMessageEvent
        waiter.waitForEvent(GenericMessageEvent.class, e -> {
            // If we're dealing with a message reaction being added we return whether it's valid
            if (e instanceof MessageReactionAddEvent)
                return isValidReaction(m, (MessageReactionAddEvent) e);
            // If we're dealing with a received message being added we return whether it's valid
            if (e instanceof MessageReceivedEvent)
                return isValidMessage(m, (MessageReceivedEvent) e);
            // Otherwise return false
            return false;
        }, e -> {
            m.delete().queue();
            // If it's a valid MessageReactionAddEvent
            if (e instanceof MessageReactionAddEvent) {
                MessageReactionAddEvent event = (MessageReactionAddEvent) e;
                // Process which reaction it is
                if (event.getReaction().getReactionEmote().getName().equals(CANCEL))
                    cancel.accept(m);
                else
                    confirm.accept(m);
            }
            // If it's a valid MessageReceivedEvent
            else if (e instanceof MessageReceivedEvent) {
                MessageReceivedEvent event = (MessageReceivedEvent) e;
                // Get the number in the message and process
                String msg = event.getMessage().getContentRaw().trim();
                if (msg.equalsIgnoreCase("cancel"))//Todo: Add custom cancellation text and confirmation
                    cancel.accept(m);
                else if (msg.equalsIgnoreCase("confirm"))
                    confirm.accept(m);
            }
        }, timeout, unit, () -> cancel.accept(m));
    }

    // Waits only for reaction input
    private void waitReactionOnly(Message m) {
        // This one is only for reactions
        waiter.waitForEvent(MessageReactionAddEvent.class, e -> {
            return isValidReaction(m, e);
        }, e -> {
            m.delete().queue();
            if (e.getReaction().getReactionEmote().getName().equals(CANCEL))
                cancel.accept(m);
            else
                confirm.accept(m);
        }, timeout, unit, () -> cancel.accept(m));
    }


    // This is where the displayed message for the ConfirmationMenu is built.
    private Message getMessage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        if (text != null)
            messageBuilder.append(text);
        if (useCustomEmbed && customEmbed != null) {
            messageBuilder.setEmbed(customEmbed);
        } else {
            messageBuilder.setEmbed(new EmbedBuilder().setColor(color)
                    .setDescription(description == null ? "" : description).build());//Todo: should fix this
        }
        return messageBuilder.build();
    }

    private boolean isValidReaction(Message m, MessageReactionAddEvent e) {
        // The message is not the same message as the menu
        if (!e.getMessageId().equals(m.getId()))
            return false;
        // The user is not valid
        if (!isValidUser(e.getUser(), e.isFromGuild() ? e.getGuild() : null))
            return false;
        // The reaction is the cancel reaction
        if (e.getReaction().getReactionEmote().getName().equals(CANCEL))
            return true;

        if (e.getReaction().getReactionEmote().getName().equals(CONTINUE))
            return true;
        return false;
    }

    private boolean isValidMessage(Message m, MessageReceivedEvent e) {
        // If the channel is not the same channel
        if (!e.getChannel().equals(m.getChannel()))
            return false;
        // Otherwise if it's a valid user or not
        return isValidUser(e.getAuthor(), e.isFromGuild() ? e.getGuild() : null);
    }

    /**
     * The {@link com.jagrosh.jdautilities.menu.Menu.Builder Menu.Builder} for
     * an {@link me.flashyreese.ozzie.api.util.ConfirmationMenu ConfirmationMenu}.
     *
     * @author Yao Chung Hu
     */
    public static class Builder extends Menu.Builder<ConfirmationMenu.Builder, ConfirmationMenu> {

        private Color color;
        private String text;
        private String description;
        private Consumer<Message> confirm = (m) -> {
        };
        private Consumer<Message> cancel = (m) -> {
        };
        private boolean allowTypedInput = true;
        private boolean useCustomEmbed = false;
        private MessageEmbed customEmbed = null;

        /**
         * Builds the {@link me.flashyreese.ozzie.api.util.ConfirmationMenu ConfirmationMenu}
         * with this Builder.
         *
         * @return The ConfirmationMenu built from this Builder.
         * @throws java.lang.IllegalArgumentException If one of the following is violated:
         *                                            <ul>
         *                                                <li>No {@link com.jagrosh.jdautilities.commons.waiter.EventWaiter EventWaiter} was set.</li>
         *                                                <li>No action {@link java.util.function.Consumer Consumer} was set.</li>
         *                                                <li>Neither text nor description were set.</li>
         *                                            </ul>
         */
        @Override
        public ConfirmationMenu build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(confirm != null, "Must provide an confirmation consumer");
            Checks.check(useCustomEmbed && customEmbed != null, "Must provide an custom embed");
            Checks.check(useCustomEmbed || (text != null || description != null), "Either text or description must be set");
            return new ConfirmationMenu(waiter, users, roles, timeout, unit, color, text, description, confirm, cancel, allowTypedInput, useCustomEmbed, customEmbed);
        }

        /**
         * Sets the {@link java.awt.Color Color} of the {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}.
         *
         * @param color The Color of the MessageEmbed
         * @return This builder
         */
        public ConfirmationMenu.Builder setColor(Color color) {
            this.color = color;
            return this;
        }

        /**
         * If {@code true}, {@link net.dv8tion.jda.api.entities.User User}s can type the number or
         * letter of the input to make their selection, in addition to the reaction option.
         *
         * @param allow {@code true} if raw text input is allowed, {@code false} if it is not
         * @return This builder
         */
        public ConfirmationMenu.Builder allowTextInput(boolean allow) {
            this.allowTypedInput = allow;
            return this;
        }

        /**
         * Sets the text of the {@link net.dv8tion.jda.api.entities.Message Message} to be displayed
         * when the {@link me.flashyreese.ozzie.api.util.ConfirmationMenu ConfirmationMenu} is built.
         *
         * <p>This is displayed directly above the embed.
         *
         * @param text The Message content to be displayed above the embed when the OrderedMenu is built
         * @return This builder
         */
        public ConfirmationMenu.Builder setText(String text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the description to be placed in an {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}.
         * <br>If this is {@code null}, no MessageEmbed will be displayed
         *
         * @param description The content of the MessageEmbed's description
         * @return This builder
         */
        public ConfirmationMenu.Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the {@link java.util.function.Consumer Consumer} to perform if the
         * {@link me.flashyreese.ozzie.api.util.ConfirmationMenu ConfirmationMenu} is confirmed.
         *
         * @param confirm The Consumer action to perform if the ButtonMenu is cancelled
         * @return This builder
         */
        public ConfirmationMenu.Builder setConfirm(Consumer<Message> confirm) {
            this.confirm = confirm;
            return this;
        }

        /**
         * Sets the {@link java.util.function.Consumer Consumer} to perform if the
         * {@link com.jagrosh.jdautilities.menu.OrderedMenu OrderedMenu} is cancelled.
         *
         * @param cancel The Consumer action to perform if the ButtonMenu is cancelled
         * @return This builder
         */
        public ConfirmationMenu.Builder setCancel(Consumer<Message> cancel) {
            this.cancel = cancel;
            return this;
        }

        public ConfirmationMenu.Builder useCustomEmbed(boolean use) {
            this.useCustomEmbed = use;
            return this;
        }

        public ConfirmationMenu.Builder setCustomEmbed(MessageEmbed embed) {
            useCustomEmbed(true);
            this.customEmbed = embed;
            return this;
        }
    }
}
