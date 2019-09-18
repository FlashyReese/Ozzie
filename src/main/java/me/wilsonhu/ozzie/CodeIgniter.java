package me.wilsonhu.ozzie;

import java.io.FileNotFoundException;

import org.fusesource.jansi.AnsiConsole;

public class CodeIgniter {

	public static void main(String[] args) throws FileNotFoundException {
		AnsiConsole.systemInstall();
		new OzzieManager(args);
	}

}
