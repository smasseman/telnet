package se.familjensmas;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.familjensmas.telnet.Command;

/**
 * @author jorgen.smas@entercash.com
 */
public class TelnetServer {

	private static Logger logger = LoggerFactory.getLogger(TelnetServer.class);
	private static TelnetServer server;

	public static void main(String[] args) throws IOException {
		server = new TelnetServer();
		server.start();

	}

	private Thread thread;

	private void start() {
		thread = new Thread() {
			@Override
			public void run() {
				ServerSocket ss = null;
				try {
					ss = new ServerSocket(45600);
					final Socket s = ss.accept();
					ConnectionThread ct = new ConnectionThread(s);
					ct.start();
				} catch (Throwable t) {
					logger.error("Server thread is down.", t);
				} finally {
					close(ss);
				}
			}

		};
		thread.start();
	}

	private class ConnectionThread extends Thread {
		private Socket s;
		private InputStream in;
		private OutputStream out;

		public ConnectionThread(Socket s) throws IOException {
			this.s = s;
			this.in = s.getInputStream();
			this.out = s.getOutputStream();

		}

		@Override
		public void run() {
			try {
				loop();
			} catch (Throwable t) {
				logger.error("Connection thread is donw.", t);
			} finally {
				close(s);
			}
		}

		private void loop() throws Exception {
			System.out.println("Got connection.");
			int input;

			write(Command.IAC, Command.DO, Command.ECHO);
			write(Command.IAC, Command.DO, Command.WINDOW_SIZE);
			write(Command.IAC, Command.WILL, Command.ECHO);
			write(Command.IAC, Command.WILL, Command.SUPRESS_GO_AHEAD);
			readAnswer();
			System.out.println("---");

			// write(Command.IAC, Command.DO, Command.WINDOW_SIZE);
			readAnswer();
			System.out.println("---");

			// write(Command.IAC, Command.DO, Command.REMOTE_FLOW_CONTROL);
			// readAnswer();
			// System.out.println("---");

			// write(Command.IAC, Command.WILL, Command.ECHO);
			readAnswer();
			System.out.println("---");

			// write(Command.IAC, Command.WILL, Command.SUPRESS_GO_AHEAD);
			readAnswer();
			System.out.println("---");

			write('H');
			write('e');
			write('l');
			write('l');
			write('o');

			while (true) {
				input = in.read();
				System.out.println("Got input: " + input);
				if (input == 'q') {
					System.exit(0);
				} else if (input == 'a') {
					out.write("You pressed a\n".getBytes());
				}
			}
		}

		private void write(Command... commands) throws IOException {
			// System.out.print("Send");
			for (Command c : commands) {
				// System.out.print(" " + c);
				out.write(c.getValue());
				System.out.println(c.getValue());
			}
			// System.out.println();
			out.flush();
		}

		private void readCommand() throws IOException {
			int i = in.read();
			Command c = Command.byValue(i);
			if (c == null)
				System.out.println("Read " + i);
			else
				System.out.println("Read " + c);
		}

		private void readAnswer() throws IOException {
			System.out.print("Read");
			int i = in.read();
			print(i);
			Command c = Command.byValue(i);
			if (c != Command.IAC) {
				logger.warn("Unexpected: " + i);
				return;
			}
			i = in.read();
			c = Command.byValue(i);
			if (c == Command.SB) {
				int last = 0;
				while (last != Command.IAC.getValue() || i != Command.SE.getValue()) {
					last = i;
					print(i);
					i = in.read();
				}
				print(i);
			} else {
				print(i);
				i = in.read();
				print(i);
			}
			System.out.println();
		}

		private void print(int i) {
			Command c = Command.byValue(i);
			if (c == null)
				System.out.print(" " + i);
			else
				System.out.print(" " + c);
		}

		private void write(int i) throws IOException {
			System.out.println("Send " + i);
			out.write(i);
			out.flush();
		}

	}

	private void close(Closeable ss) {
		if (ss != null)
			try {
				ss.close();
			} catch (IOException ignore) {
			}
	}
}
