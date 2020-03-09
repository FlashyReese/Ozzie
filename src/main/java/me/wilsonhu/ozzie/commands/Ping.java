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
package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Ping extends Command {

    public Ping() {
        super(new String[]{"ping"}, "Provides bot's latency to WebSocket, also provides user's latency to bot.", "%s");
        this.setCategory("information");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        long start = System.currentTimeMillis();
        event.getChannel().sendTyping().queue(v -> {
            long ping = System.currentTimeMillis() - start;
            EmbedBuilder embed = new EmbedBuilder()
                    .addField("Latency to " + event.getAuthor().getName(), "`" + ping + " ms`", false)
                    .addField("Latency to WebSocket", "`" + event.getJDA().getGatewayPing() + " ms`", false);

            event.getChannel().sendMessage(embed.build()).queue();
        });
    }

}