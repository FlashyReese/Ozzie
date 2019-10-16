package me.wilsonhu.ozzie.utilities;

public class OSValidator {
	public static boolean isWindows(String os) {
		return (os.indexOf("win") >= 0);
	}
	
	public static boolean isMac(String os) {
		return (os.indexOf("mac") >= 0);
	}

	public static boolean isUnix(String os) {
		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 );
	}
	
	public static boolean isSolaris(String os) {
		return (os.indexOf("sunos") >= 0);
	}
}
