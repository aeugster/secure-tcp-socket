package tools.nexus.secure_tcp_socket;

import tools.nexus.secure_tcp_socket.exceptions.SecureSocketTechnicalException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Encrypted socket for client and server use
 */
public class SecureTcpSocket extends Socket {

	private Socket providedSocket;

	private final String algorithm;
	private final SecretKey key;
	private final IvParameterSpec initVector;
	private boolean useIV = true;

	/**
	 * 'Creates a stream socket and connects it to the specified port number on the named host'
	 */
	public static SecureTcpSocket connect(String host, int port, String algorithm, SecretKey key, IvParameterSpec initVector) throws IOException {
		return new SecureTcpSocket(host, port, algorithm, key, initVector);
	}

	private SecureTcpSocket(String host, int port, String algorithm, SecretKey key, IvParameterSpec initVector) throws IOException {
		super(host, port);

		this.algorithm = algorithm;
		this.key = key;
		this.initVector = initVector;
	}

	/**
	 * Creates a {@link SecureTcpSocket} based on the provided socket
	 */
	public static SecureTcpSocket of(Socket providedSocket, String algorithm, SecretKey key, IvParameterSpec initVector) {
		return new SecureTcpSocket(providedSocket, algorithm, key, initVector);
	}

	private SecureTcpSocket(Socket providedSocket, String algorithm, SecretKey key, IvParameterSpec initVector) {
		this.providedSocket = providedSocket;

		this.algorithm = algorithm;
		this.key = key;
		this.initVector = initVector;
	}

	public void skipIv(boolean skip) {
		this.useIV = !skip;
	}

	/**
	 * get a stream for reading bytes into application
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		InputStream is = providedSocket != null ? providedSocket.getInputStream() : super.getInputStream();

		Cipher cipher;
		try {
			cipher = Cipher.getInstance(algorithm);
			if (useIV) {
				cipher.init(Cipher.DECRYPT_MODE, key, initVector);
			} else {
				cipher.init(Cipher.DECRYPT_MODE, key);
			}
		} catch (GeneralSecurityException e) {
			is.close();
			throw new IOException("InputStream - failed to init cipher: " + e.getMessage(), e);
		}

		return new CipherInputStream(is, cipher);
	}

	/**
	 * get a stream for writing bytes to socket
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		OutputStream os = providedSocket != null ? providedSocket.getOutputStream() : super.getOutputStream();
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(algorithm);
			if (useIV) {
				cipher.init(Cipher.ENCRYPT_MODE, key, initVector);
			} else {
				cipher.init(Cipher.ENCRYPT_MODE, key);
			}
		} catch (GeneralSecurityException e) {
			os.close();
			throw new IOException("OutputStream - failed to init cipher: " + e.getMessage(), e);
		}

		return new CipherOutputStream(os, cipher);
	}

	@Override
	public synchronized void close() throws IOException {
		if (providedSocket != null) {
			providedSocket.close();
		} else {
			super.close();
		}
	}

	/**
	 * generates an init vector of matching size for provided algorithm
	 *
	 * @param algorithm the algorithm which will use the IV
	 * @return the generated IV using {@link SecureRandom}
	 */
	public static IvParameterSpec getInitVector(String algorithm) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			int size = cipher.getBlockSize();
			byte[] tmp = new byte[size];

			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextBytes(tmp);
			return new IvParameterSpec(tmp);

		} catch (GeneralSecurityException e) {
			throw new SecureSocketTechnicalException("Could not setup cipher", e);
		}
	}
}