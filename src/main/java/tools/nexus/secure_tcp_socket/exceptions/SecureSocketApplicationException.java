package tools.nexus.secure_tcp_socket.exceptions;

/**
 * Application or business exception which is kind of expected
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
