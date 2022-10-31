package tools.nexus.secure_tcp_socket;

import lombok.extern.slf4j.Slf4j;
import tools.nexus.secure_tcp_socket.common.FileSerializer;
import tools.nexus.secure_tcp_socket.common.SecureTcpSocket;
import tools.nexus.secure_tcp_socket.common.SyncObjOutputStream;
import tools.nexus.secure_tcp_socket.dto.Message;
import tools.nexus.secure_tcp_socket.dto.SecSocketMessageCmd;
import tools.nexus.secure_tcp_socket.exceptions.SecureSocketTechnicalException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.util.Map;

/**
 * FortNox - Server features
 * - Among {@link SecureTcpSocket} this is the core encryption component of Nexus.
 * - Please report any security issues to related GitHub project
 * <p>
 * Purpose of this class:<br/>
 * - create keypair<br/>
 * - create symmetric key<br/>
 * - connect to the server<br/>
 * - send handshake {@link Message}s
 *
 * @author AndresE
 */
@Slf4j
public class FortNoxServer {

    /**
     * Asymmetric key algorithm
     */
    private static final String ASYM_TYPE = "RSA";

    public static final int ASYM_KEY_SIZE = 3072;
    // seems to work even cipher specifies 1024 and 2048 only
    // Consult the release documentation for your implementation to see if any
    // other transformations are supported.

    /**
     * instance variables
     */
    PrivateKey cautionPrivateKey;

    /**
     * Server: Generates an asymmetric key pair <br>
     * - Sends the public key to the client
     */
    public void action1sendPublicKey(SyncObjOutputStream trans, String keyPairLocation) {
        log.debug("Entering serverSendPublicKey1...");

        PublicKey publicKey = null;

        try {
            // Create or retrieve KeyPair
            KeyPair keyPair = generateKeyPairCached(ASYM_TYPE, ASYM_KEY_SIZE, keyPairLocation);

            publicKey = keyPair.getPublic();
            cautionPrivateKey = keyPair.getPrivate();

        } catch (NoSuchAlgorithmException e) {
            // Log on server, inform the client
            log.error("Error on setup ASYM key generator", e);
            throw new SecureSocketTechnicalException(e.getMessage());
        }

        // Send public key to client
        // no hash?
        Message m = new Message(SecSocketMessageCmd.putPubK);
        m.obj = publicKey;

        try {
            trans.writeObject(m);
        } catch (IOException e) {
            // Log on server, inform the client
            log.error("Could not send public key", e);
            throw new SecureSocketTechnicalException(e.getMessage());
        }

        log.debug("serverSendPublicKey1 DONE");
    }

    public static KeyPair generateKeyPairCached(String asymType, int asymKeySize, String keyPairLocation) throws NoSuchAlgorithmException {

        FileSerializer<KeyPair> fs = new FileSerializer<>(keyPairLocation);
        KeyPair kp = fs.getObj();

        if (kp == null) {
            kp = generateKeyPair(asymType, asymKeySize);
            fs.writeObj(kp);
        }

        return kp;
    }

    public static KeyPair generateKeyPair(String asymType, int asymSize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(asymType);

        keyPairGenerator.initialize(asymSize);
        return keyPairGenerator.genKeyPair();
    }

    /**
     * Server: Decrypts the symmetric key<br>
     * - stores it in key map <br>
     * - sends 'ready' to client
     */
    public void action3asymmetricDecryptKey(byte[] encryptedSymKey, SyncObjOutputStream objStream, Socket socket,
                                            Map<String, Object[]> keys,
                                            byte[] initVector) {
        log.debug("Entering asymmetricDecryptKey3...");

        try {
            byte[] decryptedSymKeyBytes = decrypt(encryptedSymKey, cautionPrivateKey);
            SecretKey sk = new SecretKeySpec(decryptedSymKeyBytes, FortNoxClient.SYMMETRIC_TYPE);

            String clientName = FortNoxServer.getClientHost(socket);
            log.info("PUT key in map for: '" + clientName + "'");

            // Make server ready
            Object[] keyWithVector = {sk, initVector};
            keys.put(clientName, keyWithVector);

            // Client waits for an answer
            objStream.writeObject(new Message(SecSocketMessageCmd.ready));

        } catch (IOException e) {
            throw new SecureSocketTechnicalException("Error while sending 'ready'", e);
        }
        log.debug("asymmetricDecryptKey3 DONE");
    }

    /**
     * FortNox does only asymmetric.
     */
    public static byte[] decrypt(byte[] encryptedSymKey, PrivateKey cautionPrivateKey) {
        try {
            Cipher cipher = Cipher.getInstance(FortNoxClient.ENCR_DECR_OPTIONS);
            cipher.init(Cipher.DECRYPT_MODE, cautionPrivateKey);

            return cipher.doFinal(encryptedSymKey);
        } catch (GeneralSecurityException e) {
            throw new SecureSocketTechnicalException("Error while decrypt", e);
        }
    }

    /**
     * Extracts the IP address out of given socket
     */
    public static String getClientHost(Socket socket) {
        // ipV4 pattern /127.0.0.1:50382
        // ipV6 pattern: /0:0:0:0:0:0:0:1:51149
        String addrAndPort = socket.getRemoteSocketAddress().toString();
        return extractIp(addrAndPort);
    }

    public static String extractIp(String addrAndPort) {
        int index = addrAndPort.lastIndexOf(':');
        return addrAndPort.substring(1, index);
    }

    /**
     * Creates new {@link SecureTcpSocket} - the encryption is always symmetric
     */
    @SuppressWarnings("java:S3329") // IV's should be random and unique
    public static Socket createSecureTcpSocket(Socket aClient, Object[] keyAndVector) {
        return SecureTcpSocket.of(aClient, FortNoxClient.SYMMETRIC_ALGORITHM,
                (SecretKey) keyAndVector[0],
                new IvParameterSpec((byte[]) keyAndVector[1]));
    }
}
