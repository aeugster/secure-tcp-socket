package tools.nexus.secure_tcp_socket.common;

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
import java.security.spec.AlgorithmParameterSpec;

/**
 * Encrypted socket for client and server use
 */
public class SecureTcpSocket extends Socket {

    private Socket providedSocket;

    private final String transformation;
    private final SecretKey key;
    private final AlgorithmParameterSpec dynamicInitVector;
    private boolean useIV = true;

    /**
     * 'Creates a stream socket and connects it to the specified port number on the named host'
     *
     * @param dynamicInitVector dynamically-generated IV
     */
    public static SecureTcpSocket connect(String host, int port, String algorithm, SecretKey key, IvParameterSpec dynamicInitVector) throws IOException {
        return new SecureTcpSocket(host, port, algorithm, key, dynamicInitVector);
    }

    /**
     * 'Connecting' constructor
     */
    private SecureTcpSocket(String host, int port, String transformation, SecretKey key, IvParameterSpec dynamicInitVector) throws IOException {
        super(host, port);

        this.transformation = transformation;
        this.key = key;
        this.dynamicInitVector = dynamicInitVector;
    }

    /**
     * Creates a {@link SecureTcpSocket} based on the provided socket
     */
    public static SecureTcpSocket of(Socket providedSocket, String transformation, SecretKey key, AlgorithmParameterSpec initVector) {
        return new SecureTcpSocket(providedSocket, transformation, key, initVector);
    }

    /**
     * 'Providing' constructor
     */
    private SecureTcpSocket(Socket providedSocket, String transformation, SecretKey key, AlgorithmParameterSpec dynamicInitVector) {
        this.providedSocket = providedSocket;

        this.transformation = transformation;
        this.key = key;
        this.dynamicInitVector = dynamicInitVector;
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
            cipher = Cipher.getInstance(transformation);
            if (useIV) {
                cipher.init(Cipher.DECRYPT_MODE, key, dynamicInitVector);
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
    @SuppressWarnings("java:S6432") // use dyn-generated IV
    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream os = providedSocket != null ? providedSocket.getOutputStream() : super.getOutputStream();

        Cipher cipher;
        try {
            cipher = Cipher.getInstance(transformation);
            if (useIV) {
                cipher.init(Cipher.ENCRYPT_MODE, key, dynamicInitVector);
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
