package tools.nexus.secure_tcp_socket.exceptions;

public class SecureSocketTechnicalException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SecureSocketTechnicalException(String message) {
		super(message);
	}

	public SecureSocketTechnicalException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
