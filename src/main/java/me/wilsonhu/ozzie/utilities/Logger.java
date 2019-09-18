package me.wilsonhu.ozzie.utilities;

import me.wilsonhu.ozzie.OzzieManager;

public class Logger {

	private OzzieManager manager;
	
	public Logger(OzzieManager manager) {
		setOzzieManager(manager);
		info("Logger started");
	}
	
	
	public void info(String msg) {
		System.out.println("[" + ColorLibrary.CYAN + getOzzieManager().getBotName() + ColorLibrary.RESET + "][" + ColorLibrary.GREEN + "INFO" + ColorLibrary.RESET + "] " + msg);
	}
	
	public void warn(String msg) {
		System.out.println("[" + ColorLibrary.CYAN + getOzzieManager().getBotName() + ColorLibrary.RESET + "][" + ColorLibrary.YELLOW + "WARN" + ColorLibrary.RESET + "] " + msg);
	}
	

	public void error(String msg) {
		System.out.println("[" + ColorLibrary.CYAN + getOzzieManager().getBotName() + ColorLibrary.RESET + "][" + ColorLibrary.RED + "ERROR" + ColorLibrary.RESET + "] " + msg);
	}

	public OzzieManager getOzzieManager() {
		return manager;
	}

	public void setOzzieManager(OzzieManager manager) {
		this.manager = manager;
	}
}
