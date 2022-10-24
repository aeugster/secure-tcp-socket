package tools.nexus.secure_tcp_socket;

import lombok.extern.slf4j.Slf4j;
import tools.nexus.secure_tcp_socket.common.ObjInputStream;
import tools.nexus.secure_tcp_socket.common.SyncObjOutputStream;
import tools.nexus.secure_tcp_socket.dto.Message;
import tools.nexus.secure_tcp_socket.dto.SecSocketMessageCmd;
import tools.nexus.secure_tcp_socket.exceptions.SecureSocketTechnicalException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

/**
 * FortNox - limited to Client features
 *
 * @author AndresE
 */
@Slf4j
public class FortNoxClient {

    /**
     * Encrypt and decrypt options
     * <p>
     * deprecated:                RSA/ECB/PKCS1Padding
     * sonar, provider not found: RSA/None/OAEPWithSHA-1AndMGF1Padding
     * sonar, provider not found: RSA/None/OAEPWITHSHA-256ANDMGF1PADDING
     */
    public static final String ENCR_DECR_OPTIONS = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";

    /**
     * Symmetric key algorithm .
     */
    public static final String SYMMETRIC_TYPE = "AES";

    public static final String SYMMETRIC_ALGORITHM = "AES/CTR/NoPadding";

    public static final int SYMMETRIC_KEY_SIZE = 128; // 256; Out - Failed to init
    // cipher: Illegal key size

    /**
     * Member variables
     */
    private byte[] publicKeyBytes;

    public byte[] getPublicKeyBytes() {
        return publicKeyBytes;
    }

    /**
     * Client: Request public key from server<br>
     * - Receive public from server<br>
     * - Generate symKey and encrypt it with public<br>
     * - provide initVector<br>
     * - send encrypted key "putEncSymKey"<br>
     * - close socket and connect again<br>
     * <p>
     * - TODO z feature zFatClient: Fortnox reconnect necessary?!
     *
     * @param oldSocket the old socket
     * @param trans     established ObjOutputStream
     * @return the cipher socket
     */
    public Socket setupSecureSocket2(String host, Socket oldSocket, SyncObjOutputStream trans) throws IOException {
        log.debug("Entering setupSecureSocket2...");

        Message m = new Message(SecSocketMessageCmd.getPubK);

        m.storedHash = m.hashCode();
        trans.writeObject(m);

        try {
            ObjInputStream read = new ObjInputStream(oldSocket.getInputStream());
            Message inMessage = (Message) read.readUnshared(); // blocking

            if (inMessage.obj == null) {
                throw new SecureSocketTechnicalException("Received NULL instead of public key from server");
            }

            if (!inMessage.command.equals(SecSocketMessageCmd.putPubK)) {
                throw new SecureSocketTechnicalException("Received '" + inMessage.command + "' instead of " + SecSocketMessageCmd.putPubK);
            }

            // Retrieve keys
            PublicKey publicKey = (PublicKey) inMessage.obj;
            publicKeyBytes = publicKey.getEncoded();
            Key symmetricKey = generateSymmetricKey(SYMMETRIC_TYPE, SYMMETRIC_KEY_SIZE);

            // Setup answer - key
            Message encryptedAwMessage = new Message(SecSocketMessageCmd.putEncSymKey);
            encryptedAwMessage.buffer = encrypt(symmetricKey.getEncoded(), publicKey);

            // add initVector (IvParameterSpec is not serializable)
            IvParameterSpec initVector = SecureTcpSocket.getInitVector(SYMMETRIC_TYPE);
            encryptedAwMessage.obj = initVector.getIV();

            encryptedAwMessage.storedHash = encryptedAwMessage.hashCode();
            trans.writeObject(encryptedAwMessage);

            // Client has to wait for answer "READY"
            inMessage = (Message) read.readUnshared(); // blocks
            if (inMessage.command.equals(SecSocketMessageCmd.ready)) {
                log.info("Server stored my symmetric key");
            } else {
                log.error("Server did NOT store my symmetric key");
                return null;
            }

            // Get rid of old socket
            int port = oldSocket.getPort();
            oldSocket.close();

            var result = setupSecureTcpSocket(host, port, symmetricKey, initVector);
            log.debug("setupSecureSocket2 DONE");
            return result;

        } catch (SocketException e) {
            if (e.getMessage().equals("Connection reset")) {
                throw new SecureSocketTechnicalException(
                        "Server did a reset on connection. Do you have the LATEST client downloaded? (DTO serialVersionUID)", e);
            }
            throw new SecureSocketTechnicalException("General socket error occured", e);
        } catch (GeneralSecurityException e) {
            throw new SecureSocketTechnicalException("General encryption error occured", e);
        } catch (Exception e) {
            // TODO z Guideline: NPE does not provide message which could lead to another NPE :-P
            throw new SecureSocketTechnicalException("Client could not setup Cipher socket", e);
        }
    }

    /**
     * Internal method, public for testing
     *
     * @param symType type required for testing purpose
     * @return the symmetric key
     */
    public static Key generateSymmetricKey(String symType, int symSize) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(symType);

        // If this key generator requires any random bytes, it will get them
        // using the SecureRandom implementation of the highest-priority
        // installed provider as the source of randomness.
        keyGenerator.init(symSize);

        return keyGenerator.generateKey();
    }

    private static Socket setupSecureTcpSocket(String host, int port, Key symKey, IvParameterSpec initVector) throws IOException {
        return SecureTcpSocket.connect(host, port, SYMMETRIC_ALGORITHM,
                (SecretKey) symKey,
                initVector);
    }

    /**
     * FortNox does only asymmetric.
     */
    public static byte[] encrypt(byte[] symmetric, PublicKey publicKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ENCR_DECR_OPTIONS /*, provider*/);

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(symmetric);
    }
}
