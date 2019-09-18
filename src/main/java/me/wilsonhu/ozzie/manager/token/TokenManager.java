package me.wilsonhu.ozzie.manager.token;

import java.util.HashMap;

import me.wilsonhu.ozzie.OzzieManager;

public class TokenManager {
	private OzzieManager ozzieManager;
	private HashMap<String, String> tokenList = new HashMap<String, String>();
	
	public TokenManager(OzzieManager ozzieManager) {
		this.setOzzieManager(ozzieManager);
		getOzzieManager().getLogger().info("Token Manager started");
	}
	
	public HashMap<String, String> getTokenList(){
		return tokenList;
	}

	
	public void setTokenList(HashMap<String, String> tokenList){
		this.tokenList = tokenList;
	}
	
	
	private OzzieManager getOzzieManager() {
		return ozzieManager;
	}


	private void setOzzieManager(OzzieManager ozzieManager) {
		this.ozzieManager = ozzieManager;
	}
}
