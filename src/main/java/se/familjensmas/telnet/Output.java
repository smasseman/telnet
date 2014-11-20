package se.familjensmas.telnet;

/**
 * @author jorgen.smas@entercash.com
 */
public class Output {

	public static String newLine() {
		return esc("E");
	}

	private static String esc(String string) {
		return "\u001B" + string;
	}

	private static String escBracket(String string) {
		return "\u001B[" + string;
	}

	public static String cursorLeft() {
		return escBracket("1D");
	}

	public static String cursorRight() {
		return escBracket("1C");
	}

	public static String cursorUp() {
		return escBracket("1A");
	}

	public static String cursorDown() {
		return escBracket("1B");
	}

	public static String eraseLineFromCursor() {
		return escBracket("0K");
	}
}
