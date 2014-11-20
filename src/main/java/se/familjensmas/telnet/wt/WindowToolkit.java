package se.familjensmas.telnet.wt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.familjensmas.telnet.Output;
import se.familjensmas.telnet.TelnetD.Sender;

/**
 * @author jorgen.smas@entercash.com
 */
public class WindowToolkit {

	private class Cursor {
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(getClass().getSimpleName());
			builder.append("[x=");
			builder.append(x);
			builder.append(", y=");
			builder.append(y);
			builder.append("]");
			return builder.toString();
		}

		int x;
		int y;

		void up() {
			y--;
		}

		void down() {
			y++;
		}

		void left() {
			x--;
		}

		void rigth() {
			x++;
		}
	}

	private Sender sender;
	private int columns;
	private int rows;
	private boolean inited;
	private Cursor cursor = new Cursor();
	private Logger logger = LoggerFactory.getLogger(getClass());

	public WindowToolkit(Sender sender, int columns, int rows) {
		this.sender = sender;
		this.columns = columns;
		this.rows = rows;
	}

	public synchronized void print(Text text) throws IOException {
		init();
		StringBuilder s = moveCursor(text.getX(), text.getY());
		for (char c : text.getText().toCharArray()) {
			s.append(c);
			cursor.rigth();
		}
		s.append(Output.eraseLineFromCursor());
		sender.send(s.toString());
	}

	private StringBuilder moveCursor(int x, int y) {
		logger.trace("Move cursor from " + cursor);
		StringBuilder s = new StringBuilder();
		while (cursor.x > x) {
			cursor.left();
			s.append(Output.cursorLeft());
		}
		while (cursor.x < x) {
			cursor.rigth();
			s.append(Output.cursorRight());
		}
		while (cursor.y > y) {
			cursor.up();
			s.append(Output.cursorUp());
		}
		while (cursor.y < y) {
			cursor.down();
			s.append(Output.cursorDown());
		}
		logger.trace("Moved cursor to: " + cursor);
		return s;
	}

	private void init() throws IOException {
		if (inited)
			return;
		StringBuilder s = new StringBuilder();
		for (int y = 0; y < rows; y++) {
			s.append(Output.newLine());
			cursor.down();
		}
		sender.send(s.toString());
		inited = true;
		logger.trace("Inited and cursor position is now " + cursor);
	}
}
