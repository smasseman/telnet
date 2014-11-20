package se.familjensmas.telnet;

import se.familjensmas.telnet.TelnetD.Sender;

/**
 * @author jorgen.smas@entercash.com
 */
public interface Connection {

	Sender getSender();

	void disconnect();
}
