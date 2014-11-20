package se.familjensmas.telnet;


/**
 * @author jorgen.smas@entercash.com
 */
public interface ConnectionHandler {

	void run();

	void notify(char i);

	void subnegotiation(Command option, byte[] data);

	void receivedCommand(Command command, Command option);

}
