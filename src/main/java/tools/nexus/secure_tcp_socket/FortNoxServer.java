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
import java.net.SocketAddress;
import java.security.*;
import java.util.HashMap;
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

    private static final Map<String, Object[]> HOLY_MAP_OF_KEYS = new HashMap<>();

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

        PublicKey publicKey;

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

        Message m = new Message(SecSocketMessageCmd.putPubK);
        m.obj = publicKey.getEncoded();
        // no hashing, please check key on client side

        try {
            trans.writeObject(m);
        } catch (IOException e) {
            // Log on server, inform the client
            log.error("Could not send public key", e);
            throw new SecureSocketTechnicalException(e.getMessage());
        }

        log.debug("serverSendPublicKey1 DONE");
    }

    static KeyPair generateKeyPairCached(String asymType, int asymKeySize, String keyPairLocation) throws NoSuchAlgorithmException {

        FileSerializer<KeyPair> fs = new FileSerializer<>(keyPairLocation);
        KeyPair kp = fs.getObject();

        if (kp == null) {
            kp = generateKeyPair(asymType, asymKeySize);
            fs.writeObject(kp);
        }

        return kp;
    }

    static KeyPair generateKeyPair(String asymType, int asymSize) throws NoSuchAlgorithmException {
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
                                            byte[] initVector) {
        log.debug("Entering asymmetricDecryptKey3...");

        try {
            byte[] decryptedSymKeyBytes = decrypt(encryptedSymKey, cautionPrivateKey);
            SecretKey sk = new SecretKeySpec(decryptedSymKeyBytes, FortNoxClient.SYMMETRIC_ALGORITHM);

            String clientName = FortNoxServer.getClientIdentification(socket);
            log.info("PUT key in map for: '" + clientName + "'");

            // Make server ready
            Object[] keyWithVector = {sk, initVector};
            HOLY_MAP_OF_KEYS.put(clientName, keyWithVector);

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
    static byte[] decrypt(byte[] encryptedSymKey, PrivateKey cautionPrivateKey) {
        try {
            Cipher cipher = Cipher.getInstance(FortNoxClient.ENCR_DECR_OPTIONS);
            cipher.init(Cipher.DECRYPT_MODE, cautionPrivateKey);

            return cipher.doFinal(encryptedSymKey);
        } catch (GeneralSecurityException e) {
            throw new SecureSocketTechnicalException("Error while decrypt", e);
        }
    }

    /**
     * public: currently required for lib-user the-nexus
     */
    public boolean containsKey(String clientIdentification){
        return HOLY_MAP_OF_KEYS.containsKey(clientIdentification);
    }

    /**
     * Returns a client identifier which might look like IP address of given socket
     * <p>
     * public: currently required for lib-user the-nexus
     *
     * @param socket the socket to be identified
     * @return a proprietary client identifier to use within a map
     */
    public static String getClientIdentification(Socket socket) {
        // ipV4 pattern /127.0.0.1:50382
        // ipV4 vs host nexus.tools/85.217.171.26:1234   !!
        // ipV6 pattern: /0:0:0:0:0:0:0:1:51149
        SocketAddress addrAndPort = socket.getRemoteSocketAddress();

        // extract ip, Caution: a SocketAddress with domain name leads to unexpected result
        String strAddrAndPort = addrAndPort.toString();
        int index = strAddrAndPort.lastIndexOf(':');
        return strAddrAndPort.substring(1, index);
    }

    @SuppressWarnings("java:S3329") // IV's should be random and unique
    public Socket createSecureSocketViaIdentifierRemoveKey(Socket socket) {
        String clientIdentification = getClientIdentification(socket);

        var result = SecureTcpSocket.of(socket, FortNoxClient.SYMMETRIC_TRANSFORMATION,
                (SecretKey) HOLY_MAP_OF_KEYS.get(clientIdentification)[0],
                new IvParameterSpec((byte[]) HOLY_MAP_OF_KEYS.get(clientIdentification)[1]));

        HOLY_MAP_OF_KEYS.remove(clientIdentification);

        return result;
    }
}
