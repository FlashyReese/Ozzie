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

import bsh.Interpreter;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Evaluate extends Command {

    public Evaluate() {
        super(new String[]{"evaluate"}, "Cool Stuff", "%s <java code>");
        this.setCategory("developer");
        this.setPermission("ozzie.developer");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent evt, Ozzie ozzie) throws Exception {
        try {
            Interpreter interpreter = new Interpreter();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream old = System.out;
            System.setOut(ps);
            interpreter.eval(full.replace(this.getNames()[0] + " ", ""));
            System.out.flush();
            System.setOut(old);
            evt.getTextChannel().sendTyping().queue(v -> {
                evt.getTextChannel().sendMessage(baos.toString()).queue();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}