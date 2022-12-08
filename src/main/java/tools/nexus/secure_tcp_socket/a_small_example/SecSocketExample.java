package tools.nexus.secure_tcp_socket.a_small_example;

import lombok.extern.slf4j.Slf4j;
import tools.nexus.secure_tcp_socket.common.DeprecatedWaitUtil;
import tools.nexus.secure_tcp_socket.dto.Message;
import tools.nexus.secure_tcp_socket.exceptions.SecureSocketTechnicalException;

import java.io.IOException;

/**
 * Example with client / server
 * <p>
 * - Remove <scope>test</scope> in pom.xml to have log statements printed
 */
@Slf4j
public class SecSocketExample {

    /**
     * productive server port
     */
    public static final int PROD_PORT = 1234;

    /**
     * productive frist public key byte (please compare the whole key)
     */
    public static final byte PROD_FIRST_PUBLIC_KEY_BYTE = 48;

    public static final String SERVER = "localhost";

    /**
     * test flags
     */
    private static Message testFlagReceivedMessage;

    /**
     * Arguments are parsed with {@link SecSocketArguments }
     */
    public static void main(String[] args) throws IOException {

        // start server
        var t1 = new Thread(() -> SecSocketExample.runServer(args));
        t1.start();

        // await listening of server (don't wait or sleep in your productive code)
        DeprecatedWaitUtil.waitMillis(1000, ExampleServer::isTestFlagDidListen);

        SecSocketArguments parsed = SecSocketArguments.parse(args);

        // create client and run it
        var client = new ExampleClient(SERVER, parsed.getPort(), PROD_FIRST_PUBLIC_KEY_BYTE);
        client.connectToServer();

        // use connection
        client.getOutput().writeObject(Message.createListRequest());
        Message message = (Message) client.getInput().readUnshared();
        log(message.command + " message received: " + message.name);

        testFlagReceivedMessage = message;
    }

    static void runServer(String[] args) {
        try {
            ExampleServer.main(args);
        } catch (IOException | IllegalArgumentException e) {
            throw new SecureSocketTechnicalException(e.getMessage());
        }
    }

    @SuppressWarnings("java:S106") // avoid std out
    static void log(String str) {
        System.out.println(str);
    }

    static Message getTestFlagReceivedMessage() {
        return testFlagReceivedMessage;
    }
}
