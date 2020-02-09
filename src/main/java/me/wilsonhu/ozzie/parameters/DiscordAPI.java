package me.wilsonhu.ozzie.parameters;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.parameter.Parameter;

public class DiscordAPI extends Parameter {

    public DiscordAPI() {
        super("discordapi", "discordapi <token>");
    }

    @Override
    public void onRun(String full, String split, Ozzie ozzie) throws Exception {
        ozzie.getTokenManager().addToken("discord", split, ozzie);
    }
}
