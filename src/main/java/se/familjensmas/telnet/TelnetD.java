package se.familjensmas.telnet;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jorgen.smas@entercash.com
 */
public class TelnetD {

	private Logger logger = LoggerFactory.getLogger(TelnetD.class);

	private Thread newConnectionListener;
	protected ConnectionHandlerFactory connectionHandlerFactory;

	public void start() {
		newConnectionListener = new Thread() {
			@Override
			public void run() {
				ServerSocket ss = null;
				try {
					ss = new ServerSocket(45600);
					while (true) {
						final Socket s = ss.accept();
						new Thread() {
							@Override
							public void run() {
								try {
									SenderThread sender = new SenderThread(s.getOutputStream());
									ConnectionImpl connection = new ConnectionImpl();
									connection.sender = sender;
									ConnectionHandler h = connectionHandlerFactory.create(connection);
									ReaderThread reader = new ReaderThread(s.getInputStream(), h);
									reader.start();
									connection.reader = reader;
									connection.socket = s;
									h.run();
								} catch (IOException e) {
									logger.error("Failure in connection.", e);
								}
							}
						}.start();
					}
				} catch (Throwable t) {
					logger.error("Server thread is down.", t);
				} finally {
					TelnetD.this.close(ss);
				}
			}

		};
		newConnectionListener.start();
	}

	public interface Sender {

		public void send(int c) throws IOException;

		public void send(Command... commands) throws IOException;

		public void send(String string) throws IOException;
	}

	private class SenderThread implements Sender {

		private OutputStream out;

		private SenderThread(OutputStream out) {
			super();
			this.out = out;
		}

		@Override
		public void send(int c) throws IOException {
			out.write(c);
			print("Send char: " + c);
		}

		@Override
		public void send(Command... commands) throws IOException {
			StringBuilder s = new StringBuilder("Send");
			for (Command c : commands) {
				s.append(" " + c);
			}
			print(s.toString());
			for (Command c : commands) {
				out.write(c.getValue());
			}
			out.flush();
		}

		@Override
		public void send(String string) throws IOException {
			for (char c : string.toCharArray()) {
				send(c);
			}
		}

	}

	private class ReaderThread extends Thread {
		private InputStream in;
		private ConnectionHandler h;

		public ReaderThread(InputStream in, ConnectionHandler h) throws IOException {
			this.in = in;
			this.h = h;
		}

		@Override
		public void run() {
			try {
				while (!interrupted()) {
					int i = in.read();
					if (Command.IAC.getValue() == i) {
						i = in.read();
						if (Command.SB.getValue() == i) {
							int last = 0;
							i = 0;
							Command dataType = Command.byValue(in.read());
							LinkedList<Integer> data = new LinkedList<>();
							while (last != Command.IAC.getValue() || i != Command.SE.getValue()) {
								last = i;
								i = in.read();
								data.add(i);
							}
							data.removeLast();
							data.removeLast();
							byte[] bytes = new byte[data.size()];
							Iterator<Integer> iter = data.iterator();
							int counter = 0;
							while (iter.hasNext()) {
								bytes[counter++] = (byte) ((0xFF) & iter.next());
							}
							subnegotiation(dataType, bytes);
						} else {
							Command command = Command.byValue(i);
							Command option = Command.byValue(in.read());
							receivedCommand(command, option);
						}
					} else {
						print("Received data char: " + i);
						h.notify((char) i);
					}
				}
			} catch (Throwable t) {
				if (!interrupted())
					logger.error("Reader is down.", t);
			}
		}

		private void receivedCommand(Command command, Command option) {
			print("Received command and option: " + command + " " + option);
			h.receivedCommand(command, option);
		}

		private void subnegotiation(Command option, byte[] bytes) {
			StringBuilder s = new StringBuilder();
			s.append("Received subnegotiation for " + option + ":");
			for (int i : bytes) {
				s.append(" " + i);
			}
			print(s.toString());
			h.subnegotiation(option, bytes);
		}
	}

	private synchronized void print(String string) {
		logger.trace(string);
	}

	private void close(Closeable ss) {
		if (ss != null)
			try {
				ss.close();
			} catch (IOException ignore) {
			}
	}

	public void setConnectionHandlerFactory(ConnectionHandlerFactory connectionHandlerFactory) {
		this.connectionHandlerFactory = connectionHandlerFactory;
	}

	private class ConnectionImpl implements Connection {

		protected Socket socket;
		protected ReaderThread reader;
		private Sender sender;

		@Override
		public Sender getSender() {
			return sender;
		}

		@Override
		public void disconnect() {
			reader.interrupt();
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}
}
