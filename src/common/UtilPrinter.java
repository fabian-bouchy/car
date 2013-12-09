package common;

public class UtilPrinter {
	
	private static final String ANSI_RESET = "\u001B[0m";
	@SuppressWarnings("unused")
	private static final String ANSI_BLACK = "\u001B[30m";
	private static final String ANSI_RED = "\u001B[31m";
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_YELLOW = "\u001B[33m";
	private static final String ANSI_BLUE = "\u001B[34m";
	@SuppressWarnings("unused")
	private static final String ANSI_PURPLE = "\u001B[35m";
	@SuppressWarnings("unused")
	private static final String ANSI_CYAN = "\u001B[36m";
	@SuppressWarnings("unused")
	private static final String ANSI_WHITE = "\u001B[37m";
	
	public static void printlnSucceed(String line) {
		System.out.println(ANSI_GREEN + line + ANSI_RESET);
	}
	
	public static void printlnError(String line) {
		System.out.println(ANSI_RED + line + ANSI_RESET);
	}
	
	public static void printlnWarning(String line) {
		System.out.println(ANSI_YELLOW + line + ANSI_RESET);
	}
	
	public static void printlnQuestion(String line) {
		System.out.println(ANSI_BLUE + line + ANSI_RESET);
	}
}
