package git.util;

public class Platform {
	
	public static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

	public static boolean isWindows() {
		return WINDOWS;
	}
	
}