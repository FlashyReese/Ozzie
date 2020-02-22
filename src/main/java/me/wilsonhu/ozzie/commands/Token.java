package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Token extends Command {
    public Token() {
        super(new String[]{"token"}, "Token Manager", "%s add <Token Name> <Token> | token remove <Token Name>");
        this.setPermission("ozzie.modifytoken");
        this.setCategory("developer");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(full.equalsIgnoreCase(args[0])) {
            event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
            return;
        }
        if(args[1].equalsIgnoreCase("add")) {
            if(args.length == 4) {
                String name = args[2];
                String token = args[3];
                ozzie.getTokenManager().addToken(name, token, ozzie);//getTokenList().put(name, token);
                event.getChannel().sendMessage(String.format("The token `%s` with the value `%s` has been added.", name, token)).queue();
            }else {
                event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
                return;
            }
        }
        if(args[1].equalsIgnoreCase("remove")) {
            if(args.length == 3) {
                String name = args[2];
                String tokenValue = ozzie.getTokenManager().getToken(name);
                ozzie.getTokenManager().removeToken(name, ozzie);
                event.getChannel().sendMessage(String.format("The token `%s` with the value `%s` has been removed.", name, tokenValue)).queue();
            }else {
                event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
            }
        }
    }
}