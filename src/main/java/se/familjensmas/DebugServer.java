package se.familjensmas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author jorgen.smas@entercash.com
 */
public class DebugServer {

	public static class MyThread extends Thread {
		private MyThread(InputStream in, OutputStream out, String prefix) {
			super();
			this.in = in;
			this.out = out;
			this.prefix = prefix;
		}

		InputStream in;
		OutputStream out;
		String prefix;

		@Override
		public void run() {
			try {
				while (true) {
					int input;
					while (true) {
						input = read(in);
						delay(5);
						out(prefix, input);
						out.write(input);
						delay(5);
					}
				}
			} catch (IOException e) {

			}
		}

		private static int read(InputStream in) throws IOException {
			delay(10);
			return in.read();
		}

		private static void delay(int i) {
			try {
				Thread.sleep(i);
			} catch (InterruptedException e) {
			}
		}

	}

	static String lastPrefix;

	private synchronized static void out(String prefix2, int input) {
		if (prefix2.equals("<")) {
			if (!prefix2.equals(lastPrefix))
				System.out.print("\n" + prefix2);
			lastPrefix = prefix2;
			System.out.print(" " + (input & 0xFF));
		}
	}

	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(45600);
		Socket client = ss.accept();
		InputStream clientIn = client.getInputStream();
		OutputStream clientOut = client.getOutputStream();
		Socket serverSocket = new Socket("192.168.2.54", 23);
		InputStream serverIn = serverSocket.getInputStream();
		OutputStream serverOut = serverSocket.getOutputStream();

		new MyThread(clientIn, serverOut, ">").start();
		new MyThread(serverIn, clientOut, "<").start();
	}
}
