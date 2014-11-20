package se.familjensmas.telnet;

/**
 * @author jorgen.smas@entercash.com
 */
public enum Command {

	ECHO(1),

	SUPRESS_GO_AHEAD(3),

	STATUS(5),

	TIMING_MARK(6),

	TERMINAL_TYPE(24),

	WINDOW_SIZE(31),

	TERMINAL_SPEED(32),

	REMOTE_FLOW_CONTROL(33),

	LINE_MODE(34),

	ENVIRONMENT_VARIABLES(36),

	SE(240),

	SB(250),

	WILL(251),

	WONT(252),

	DO(253),

	DONT(254),

	IAC(255);
	private int value;

	Command(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static Command byValue(int i) {
		for (Command c : values()) {
			if (c.value == i)
				return c;
		}
		return null;
	}
}
