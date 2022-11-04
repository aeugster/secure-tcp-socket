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
public class SecSockeExample {

    /**
     * productive server port
     */
    public static final int PORT = 1234;

    public static final String SERVER = "localhost";

    /**
     * test flags
     */
    private static Message testFlagReceivedMessage;

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // start server
        var t1 = new Thread(() -> SecSockeExample.runServer(args));
        t1.start();

        // await listening of server (don't wait or sleep in your productive code)
        DeprecatedWaitUtil.waitMillis(1000, ExampleServer::isTestFlagDidListen);

        // create client and run it
        var client = new ExampleClient(SERVER, PORT);
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

    static void log(String str) {
        System.out.println(str);
    }

    static Message getTestFlagReceivedMessage() {
        return testFlagReceivedMessage;
    }
}
