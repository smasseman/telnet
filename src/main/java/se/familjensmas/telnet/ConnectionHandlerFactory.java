package se.familjensmas.telnet;


/**
 * @author jorgen.smas@entercash.com
 */
public interface ConnectionHandlerFactory {

	ConnectionHandler create(Connection connection);

}
