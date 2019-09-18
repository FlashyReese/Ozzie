package me.wilsonhu.ozzie.manager.parameter;

import me.wilsonhu.ozzie.OzzieManager;

public abstract class Param {
	private String command;
	private String syntax;
	
	public Param(String command, String syntax)
	{
		this.command = command;
		this.syntax = syntax;
	}
	
	public abstract void onCommand(String full, String[] split, OzzieManager yucenia) throws Exception;
	
	public String getCommand()
	{
		return command;
	}
	
	public String getSyntax()
	{
		return syntax;
	}
	
}
