package se.familjensmas;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.familjensmas.telnet.Command;
import se.familjensmas.telnet.Connection;
import se.familjensmas.telnet.ConnectionHandler;
import se.familjensmas.telnet.ConnectionHandlerFactory;
import se.familjensmas.telnet.TelnetD;
import se.familjensmas.telnet.wt.Text;
import se.familjensmas.telnet.wt.WindowToolkit;

/**
 * @author jorgen.smas@entercash.com
 */
public class GyroServer {

	private Logger logger = LoggerFactory.getLogger(GyroServer.class);

	public static void main(String[] args) throws InterruptedException {
		GyroServer s = new GyroServer();
		s.start();
		Thread.sleep(Long.MAX_VALUE);
	}

	public void start() {
		TelnetD s = new TelnetD();
		s.setConnectionHandlerFactory(new ConnectionHandlerFactory() {

			@Override
			public ConnectionHandler create(final Connection connection) {

				return new ConnectionHandler() {

					WindowToolkit wt;
					boolean quit;
					int wanted;

					@Override
					public void notify(char i) {
						try {
							if (i == 'q') {
								quit = true;
								connection.disconnect();
							} else if (i == 'a') {
								connection.getSender().send('A');
							} else if (i == '+') {
								wanted++;
								updateDesiredServoPosition();
							} else if (i == '-') {
								wanted--;
								updateDesiredServoPosition();
							} else if (i == 'b') {
								connection.getSender().send("\u001B[1D");
							} else {
								System.out.println("Ignore " + i);
							}
						} catch (IOException e) {
							if (!quit)
								logger.error("Failed to send.", e);
						}
					}

					@Override
					public void subnegotiation(Command option, byte[] data) {
						if (option == Command.TERMINAL_TYPE) {
							logger.debug("Term type: " + new String(data, 1, data.length - 1));
						}
					}

					@Override
					public void receivedCommand(Command command, Command option) {
					}

					@Override
					public void run() {
						try {
							send(Command.IAC, Command.DO, Command.ECHO);
							send(Command.IAC, Command.DO, Command.WINDOW_SIZE);
							send(Command.IAC, Command.WILL, Command.ECHO);
							send(Command.IAC, Command.WILL, Command.SUPRESS_GO_AHEAD);
							send(Command.IAC, Command.DO, Command.TERMINAL_TYPE);
							send(Command.IAC, Command.SB, Command.TERMINAL_TYPE, Command.ECHO, Command.IAC, Command.SE);

							wt = new WindowToolkit(connection.getSender(), 40, 3);
							updateDesiredServoPosition();
							for (int servo = -180; servo < 181; servo++) {
								updateServoPosition(servo);
								sleepOneSecond();
							}
						} catch (IOException ex) {
							if (!quit)
								ex.printStackTrace();
						}
					}

					private void updateDesiredServoPosition() throws IOException {
						StringBuilder s = new StringBuilder();
						s.append("Wanted position: ");
						if (wanted < 100 && wanted > -100)
							s.append(" ");
						s.append(wanted);
						wt.print(new Text(0, 1, s.toString()));
					}

					private void updateServoPosition(int i) throws IOException {
						StringBuilder s = new StringBuilder();
						s.append("Servo position : ");
						if (i < 100 && i > -100)
							s.append(" ");
						s.append(i);
						wt.print(new Text(0, 0, s.toString()));
					}

					private void sleepOneSecond() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}

					private void send(char c) throws IOException {
						connection.getSender().send(c);
					}

					public void send(Command... commands) throws IOException {
						connection.getSender().send(commands);
					}
				};
			}

		});
		s.start();
	}
}
