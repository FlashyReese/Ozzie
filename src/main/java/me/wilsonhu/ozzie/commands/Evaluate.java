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
    public void onCommand(String full, String split, MessageReceivedEvent evt, Ozzie ozzie) throws Exception {
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