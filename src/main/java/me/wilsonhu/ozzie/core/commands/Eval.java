package me.wilsonhu.ozzie.core.commands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import bsh.Interpreter;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import me.wilsonhu.ozzie.manager.command.CommandCategory;
import me.wilsonhu.ozzie.manager.command.CommandLevel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Eval extends Command{

	public Eval() {
		super(new String[]{"eval"}, "Cool Stuff", "%s <java code>");
		this.setLevel(CommandLevel.DEVELOPER);
		this.setCategory(CommandCategory.DEVELOPER);
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
