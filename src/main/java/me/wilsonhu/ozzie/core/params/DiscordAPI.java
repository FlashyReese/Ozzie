package me.wilsonhu.ozzie.core.params;

import me.wilsonhu.ozzie.OzzieManager;
import me.wilsonhu.ozzie.manager.parameter.Param;

public class DiscordAPI extends Param{

	public DiscordAPI() {
		super("bottoken", "bottoken <Discord API>");
	}

	@Override
	public void onCommand(String full, String[] split, OzzieManager yucenia) throws Exception {
		if(full.contains(" ") && !yucenia.getTokenManager().getTokenList().containsKey("jda")) {
			yucenia.getTokenManager().getTokenList().put("jda", split[1]);
			yucenia.getLogger().info("Successfully set Discord API Token via parameter");
			return;
		}
	}
}
