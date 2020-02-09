package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.PrintWriter;

public class Token extends Command {
    public Token() {
        super(new String[]{"token"}, "Token Manager", "%s add <Token Name> <Token> | token remove <Token Name>");
        this.setPermission("ozzie.modifytoken");
        this.setCategory("developer");
    }

    @Override
    public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(full.trim().equalsIgnoreCase(split)) {
            event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
            return;
        }
        if(split.toLowerCase().startsWith("add")) {
            String[] parameters = split.substring("add".length()).trim().split(" ");
            if(parameters.length == 2) {
                String name = parameters[0];
                String token = parameters[1];
                ozzie.getTokenManager().addToken(name, token, ozzie);//getTokenList().put(name, token);
                event.getChannel().sendMessage(String.format("The token `%s` with the value `%s` has been added.", name, token)).queue();
            }else {
                event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
                return;
            }
        }
        if(split.toLowerCase().startsWith("remove")) {
            String[] parameters = split.substring("remove".length()).trim().split(" ");
            if(parameters.length == 1) {
                String name = parameters[0];
                String tokenValue = ozzie.getTokenManager().getToken(name);
                ozzie.getTokenManager().removeToken(name, ozzie);
                event.getChannel().sendMessage(String.format("The token `%s` with the value `%s` has been removed.", name, tokenValue)).queue();
            }else {
                event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
                return;
            }
        }
    }

    @Override
    public void onCommand(String full, String split, PrintWriter writer, Ozzie ozzie) throws Exception {

    }
}