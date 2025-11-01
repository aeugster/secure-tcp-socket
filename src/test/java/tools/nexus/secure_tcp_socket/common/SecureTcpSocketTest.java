package tools.nexus.secure_tcp_socket.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.nexus.secure_tcp_socket.FortNoxClient;
import tools.nexus.secure_tcp_socket.exceptions.SecureSocketTechnicalException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Left connects to right and sends data.
 *
 * @author AndresE
 */
public class SecureTcpSocketTest {

    private static final String NONE = "none";

    /**
     * both sockets
     */
    Socket clientSocket;
    Socket serverSideSocket;

    /**
     * both streams
     */
    OutputStream output;
    InputStream input;

    /**
     * Parameters
     */
    private String symmetricAlgorithm;
    private String symmetricTransformation;
    private boolean skipIv;

    /**
     * parameter (method) source
     */
    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                // sendTwoBytes and send10Bytes fail cause of ECB
                // Junit5 issue? timeout does not work here (when skipIV change to true :-/
                // Arguments.of("Blowfish", "Blowfish/ECB/PKCS5Padding", false, true),

                Arguments.of(NONE, NONE /*                     */, false /* skip IV */),
                Arguments.of("AES", "AES/CTR/NoPadding" /*     */, false),

                // WIP
                Arguments.of("AES", "AES/GCM/NoPadding" /*     */, false),

                Arguments.of("ARCFOUR", "ARCFOUR" /*           */, true),
                Arguments.of("Blowfish", "Blowfish/CTR/NoPadding", false)
        );
    }

    private boolean isCipher() {
        return !symmetricAlgorithm.equals(NONE);
    }

    // @BeforeEach would be too early because params come afterwards :-/
    private void initAndSetup(String algorithm, String algo, boolean skipIv) throws Exception {
        this.symmetricAlgorithm = algorithm;
        this.symmetricTransformation = algo;
        this.skipIv = skipIv;

        // setup
        ServerSocket serverSocket = new ServerSocket(12345);

        clientSocket = new Socket("localhost", 12345);
        serverSideSocket = serverSocket.accept();
        serverSocket.close();

        if (isCipher()) {
            setupCipherSocket();
        } else {
            setupBaseSocket();
        }
    }

    private void setupCipherSocket() throws IOException {
        SecretKey secKey = getSymmetricTestingKey(symmetricAlgorithm, FortNoxClient.SYMMETRIC_KEY_SIZE);

        SecureTcpSocket aCypherSocket = SecureTcpSocket.of(clientSocket, symmetricTransformation, secKey,
                getInitVectorForTesting(symmetricTransformation));
        aCypherSocket.skipIv(skipIv);
        input = aCypherSocket.getInputStream();

        aCypherSocket = SecureTcpSocket.of(serverSideSocket, symmetricTransformation, secKey,
                getInitVectorForTesting(symmetricTransformation));
        aCypherSocket.skipIv(skipIv);
        output = aCypherSocket.getOutputStream();
    }

    public static SecretKey getSymmetricTestingKey(String algorithm, int symmetricKeySize) {
        if (symmetricKeySize != 128) {
            return null;
        }

        byte[] bytes;

        switch (algorithm) {
            case "AES": // 16 byte & 128 bits
                bytes = new byte[]{46, 58, 80, 29, -119, 50, -1, -86, 29, 114, -75, 44, -2, 69, 74, -77};
                break;
            case "ARCFOUR":
                bytes = new byte[]{106, -82, -15, 44, 13, 87, 103, 50, -127, 82, -83, 114, -25, 10, -36, -92};
                break;
            case "Blowfish":
                bytes = new byte[]{-18, 14, -8, 4, 32, -114, -30, 33, 41, 112, 1, -107, 77, -123, 73, -26};
                break;
            default:
                return null;
        }

        return new SecretKeySpec(bytes, algorithm);
    }

    private void setupBaseSocket() throws IOException {
        output = clientSocket.getOutputStream();
        input = serverSideSocket.getInputStream();
    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    @Timeout(1)
    void testTwoSingleBytes(String algorithm, String transformation, boolean skipIV) throws Exception {
        initAndSetup(algorithm, transformation, skipIV);

        output.write("x".getBytes(StandardCharsets.UTF_8));
        output.write("y".getBytes(StandardCharsets.UTF_8));

        byte[] array = new byte[4];
        input.read(array);
        Assertions.assertArrayEquals(new byte[]{120, 121, 0, 0}, array);
    }


    @ParameterizedTest
    @MethodSource("provideParameters")
    @Timeout(1)
    void testSend10Bytes(String algorithm, String transformation, boolean skipIV) throws Exception {
        initAndSetup(algorithm, transformation, skipIV);

        output.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

        byte[] array = new byte[12];
        input.read(array);

        Assertions.assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 0}, array);
    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    @Timeout(2)
    void testSend10kiloBytes(String algorithm, String transformation, boolean skipIV) throws Exception {
        initAndSetup(algorithm, transformation, skipIV);

        int size = 10000;
        byte[] tmp = new byte[size];

        output.write(tmp);
        output.flush();

        // at least one byte is read and stored into b
        int bytesRead = input.read(tmp);

        if (bytesRead < 500) { // Erfahrungswert
            Assertions.fail("First 500 failed");
        } else if (bytesRead == size) {
            // All bytes found - this applies only to standard stream
            return;
        }

        bytesRead = input.read(tmp);
        if (bytesRead < 500) {
            Assertions.fail("Second 500 failed with: " + bytesRead);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        // close inner
        input.close();
        output.close();

        clientSocket.close();
        serverSideSocket.close();
    }

    @SuppressWarnings("java:S3329") // IV's should be random and unique
    public static AlgorithmParameterSpec getInitVectorForTesting(String algorithm) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            int size = cipher.getBlockSize();
            byte[] tmp = new byte[size];
            Arrays.fill(tmp, (byte) 15);

            if (algorithm.contains("GCM")) {
                return new GCMParameterSpec(128, tmp);
            }
            return new IvParameterSpec(tmp);

        } catch (Exception e) {
            throw new SecureSocketTechnicalException("Could not setup cipher", e);
        }
    }
}
