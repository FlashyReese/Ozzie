package me.wilsonhu.ozzie.manager.parameter;

import java.util.ArrayList;

import me.wilsonhu.ozzie.OzzieManager;
import me.wilsonhu.ozzie.core.params.DisablePlugins;
import me.wilsonhu.ozzie.core.params.DiscordAPI;
import me.wilsonhu.ozzie.core.params.ShardSize;

public class ParameterManager {
	private OzzieManager ozziemanager;
	public String splitPrefix = "--";
	public ArrayList<String> parameters = new ArrayList<String>();
	public ArrayList<Param> actualParams = new ArrayList<Param>();
	
	public ParameterManager(OzzieManager ozziemanager, String[] args) {
		this.setOzzieManager(ozziemanager);
		loadParams();
		String allParams = "";
		for(String param: args) {
			allParams = allParams + param + " ";
		}
		for(String params: allParams.split(splitPrefix)) {
			parameters.add(splitPrefix + params);
		}
		for(String params: getParameters()) {
			onCommand(params);
		}
		getOzzieManager().getLogger().info("Parameter Manager started");
	}
	
	void loadParams() {
		this.getActualParams().add(new DiscordAPI());
		this.getActualParams().add(new DisablePlugins());
		this.getActualParams().add(new ShardSize());
	}
	
	
	public Param getParam(Class<?extends Param> leCommandClass)
	{
		for (Param c: getActualParams())
		{
			if (c.getClass() == leCommandClass)
			{
				return c;
			}
		}
		return null;
	} 
	
	void onCommand(String full){
		try
		{
			full = full.trim();
			if (full == splitPrefix)
			{
				
				//ozzie.getOzzieManager().getLogger().log(); wwat???
				return;
			}
			full = full.substring(splitPrefix.length());
			String[] s;
			if (full.contains(" "))
				s = full.split(" ");
			else
				s = new String[]{full};
			
			for (Param c: getActualParams())
			{
				if (c.getCommand().equalsIgnoreCase(s[0]))
				{
					try
					{
						c.onCommand(full, s, this.getOzzieManager());
					}
					catch (Exception e)
					{
						//ozzie.getOzzieManager().getLogger().log("Usage: " + c.getSyntax());
					}
					return;
				}
			}
			//ozzie.getOzzieManager().getLogger().log("Could not find command \"." + s[0] + "\"!");
		}
		catch (Exception e)
		{
			//ozzie.getOzzieManager().getLogger().log("Ensalada are better den zrb");
		}
	}
	
	private void setOzzieManager(OzzieManager ozziemanager) {
		this.ozziemanager = ozziemanager;
	}
	
	private OzzieManager getOzzieManager() {
		return ozziemanager;
	}
	
	public ArrayList<String> getParameters(){
		return parameters;
	}
	
	public ArrayList<Param> getActualParams(){
		return actualParams;
	}
}
