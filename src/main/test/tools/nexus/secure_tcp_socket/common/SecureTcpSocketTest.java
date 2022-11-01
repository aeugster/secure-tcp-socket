package tools.nexus.secure_tcp_socket.common;

import org.junit.jupiter.api.*;
import tools.nexus.secure_tcp_socket.FortNoxClient;
import tools.nexus.secure_tcp_socket.exceptions.SecureSocketTechnicalException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Test CipherSocket: Left connects to right and sends data.
 *
 * @author AndresE
 */
@RunWith(Parameterized.class)
class SecureTcpSocketTest {

    private static final boolean addExperimentalBlowfish = false;
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
    private final String SYM_TYPE;
    private final String SYM_ALGORITHM;
    private final boolean SKIP_IV;

    /**
     * First, setup params
     */
    @Parameters(name = "{1}-encryption")
    public static Collection<Object[]> params() {

        List<Object[]> params = new ArrayList<Object[]>(Arrays.asList(new Object[][]{ //
                {NONE, NONE /*                     */, false /* skip IV */}, //
                {"AES", "AES/CTR/NoPadding" /*     */, false}, //
                {"ARCFOUR", "ARCFOUR" /*           */, true}, //
                {"Blowfish", "Blowfish/CTR/NoPadding", false}}));

        if (addExperimentalBlowfish) {
            // sendTwoBytes and send10Bytes fail cause of ECB
            params.add((new Object[]{"Blowfish", "Blowfish/ECB/PKCS5Padding", false, true}));
        }

        return params;
    }

    /**
     * Second, the params are provided
     */
    public SecureTcpSocketTest(String type, String algo, boolean skipIV) {
        SYM_TYPE = type;
        SYM_ALGORITHM = algo;
        SKIP_IV = skipIV;
    }

    private boolean isCipher() {
        return !SYM_TYPE.equals(NONE);
    }

    /**
     * Third, setup based on params
     */
    @BeforeEach
    public void setUp() throws Exception {
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

    private void setupCipherSocket() throws NoSuchAlgorithmException, IOException {
        SecretKey secKey = (SecretKey) FortNoxClient.generateSymmetricKey(SYM_TYPE, FortNoxClient.SYMMETRIC_KEY_SIZE);

        @SuppressWarnings("resource") // wrappee is closed during tear down
        SecureTcpSocket aCypherSocket = SecureTcpSocket.of(clientSocket, SYM_ALGORITHM, secKey,
                getInitVectorForTesting(SYM_ALGORITHM));
        aCypherSocket.skipIv(SKIP_IV);
        input = aCypherSocket.getInputStream();

        aCypherSocket = SecureTcpSocket.of(serverSideSocket, SYM_ALGORITHM, secKey,
                getInitVectorForTesting(SYM_ALGORITHM));
        aCypherSocket.skipIv(SKIP_IV);
        output = aCypherSocket.getOutputStream();
    }

    private void setupBaseSocket() throws IOException {
        output = clientSocket.getOutputStream();
        input = serverSideSocket.getInputStream();
    }

    @Test
    @Timeout(1)
    void testTwoSingleBytes() throws IOException {
        output.write("x".getBytes(StandardCharsets.UTF_8));
        output.write("y".getBytes(StandardCharsets.UTF_8));

        byte[] array = new byte[4];
        input.read(array);
        Assertions.assertArrayEquals(new byte[]{120, 121, 0, 0}, array);
    }

    @Test
    @Timeout(1)
    void testSend10Bytes() throws IOException {
        output.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

        byte[] array = new byte[12];
        input.read(array);

        Assertions.assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 0}, array);
    }

    @Test
    @Timeout(2)
    void testSend10kiloBytes() throws IOException {
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

    public static IvParameterSpec getInitVectorForTesting(String algorithm) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            int size = cipher.getBlockSize();
            byte[] tmp = new byte[size];

            Arrays.fill(tmp, (byte) 15);
            return new IvParameterSpec(tmp);

        } catch (Exception e) {
            throw new SecureSocketTechnicalException("Could not setup cipher", e);
        }
    }
}
