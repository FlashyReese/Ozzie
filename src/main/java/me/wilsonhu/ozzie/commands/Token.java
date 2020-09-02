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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Token extends Command {
    public Token() {
        super(new String[]{"token"}, "Token Manager", "%s add <Token Name> <Token> | token remove <Token Name>");
        this.setPermission("ozzie.developer");
        this.setCategory("developer");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if (full.equalsIgnoreCase(args[0])) {
            event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
            return;
        }
        if (isCommand(args, "add")) {
            if (args.length == 3) {
                String name = args[1];
                String token = args[2];
                ozzie.getTokenManager().addToken(name, token);//getTokenList().put(name, token);
                event.getChannel().sendMessage(String.format("The token `%s` with the value `%s` has been added.", name, token)).queue();
            } else {
                event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
                return;
            }
        } else if (isCommand(args, "remove")) {
            if (args.length == 2) {
                String name = args[1];
                String tokenValue = ozzie.getTokenManager().getToken(name);
                ozzie.getTokenManager().removeToken(name);
                event.getChannel().sendMessage(String.format("The token `%s` with the value `%s` has been removed.", name, tokenValue)).queue();
            } else {
                event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
            }
        }
        if (isCommand(args, "reload")) {
            event.getChannel().sendMessage("Reloading saved tokens...").queue();
            ozzie.getTokenManager().loadSavedTokens();
            event.getChannel().sendMessage("Saved tokens reloaded.").queue();
        }
    }
}