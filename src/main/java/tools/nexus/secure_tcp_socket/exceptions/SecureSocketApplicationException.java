package tools.nexus.secure_tcp_socket.exceptions;

/**
 * Fach- oder Businessfehler which is kind of expected
 * - does result in warning (errorhandler)
 * - can / should be shown to the user
 */
public class SecureSocketApplicationException extends Exception {
	private static final long serialVersionUID = 1L;

	public SecureSocketApplicationException(String message) {
		super(message);
	}

	public SecureSocketApplicationException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
