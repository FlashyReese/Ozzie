package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Token extends Command {
    public Token() {
        super(new String[]{"token"}, "Token Manager", "%s add <Token Name> <Token> | token remove <Token Name>");
        this.setPermission("ozzie.developer");
        this.setCategory("developer");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(full.equalsIgnoreCase(args[0])) {
            event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
            return;
        }
        if(isCommand(args, "add")){
            if(args.length == 3) {
                String name = args[1];
                String token = args[2];
                ozzie.getTokenManager().addToken(name, token, ozzie);//getTokenList().put(name, token);
                event.getChannel().sendMessage(String.format("The token `%s` with the value `%s` has been added.", name, token)).queue();
            }else {
                event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
                return;
            }
        }else if(isCommand(args, "remove")){
            if(args.length == 2) {
                String name = args[1];
                String tokenValue = ozzie.getTokenManager().getToken(name);
                ozzie.getTokenManager().removeToken(name, ozzie);
                event.getChannel().sendMessage(String.format("The token `%s` with the value `%s` has been removed.", name, tokenValue)).queue();
            }else {
                event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
            }
        }if(isCommand(args, "reload")){
            event.getChannel().sendMessage("Reloading saved tokens...").queue();
            ozzie.getTokenManager().loadSavedTokens(ozzie);
            event.getChannel().sendMessage("Saved tokens reloaded.").queue();
        }
    }
}