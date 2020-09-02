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
import me.wilsonhu.ozzie.utilities.Tenor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Random;

public class Clara extends Command {

    public String[] tags = new String[]{"Clara Oswald", "Clara Who", "Clara Oswin", "Oswin Oswald", "Clara Oswin Oswald", "Doctor Who Clara Oswald", "Doctor Who Clara Oswin Oswald", "Doctor Who Oswin Oswald", "Doctor Who Clara Oswald"};

    public Clara() {
        super(new String[]{"clara"}, ":p much love clara", "%s");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) {
        Tenor tenor = new Tenor();
        Random r = new Random();
        int randomNumber = r.nextInt(tags.length);
        int index = r.nextInt(tenor.getItems(tags[randomNumber], ozzie).size() - 1);
        String s = tenor.getItems(tags[randomNumber], ozzie).get(index);
        event.getChannel().sendMessage(s).queue();
    }
}