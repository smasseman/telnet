package se.familjensmas.telnet.wt;

/**
 * @author jorgen.smas@entercash.com
 */
public class Text {

	private int x;
	private int y;
	private String text;

	public Text(int x, int y, String text) {
		super();
		this.x = x;
		this.y = y;
		this.text = text;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getText() {
		return text;
	}

}
