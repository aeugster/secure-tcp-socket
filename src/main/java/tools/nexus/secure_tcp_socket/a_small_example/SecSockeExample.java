package tools.nexus.secure_tcp_socket.a_small_example;

import lombok.extern.slf4j.Slf4j;
import tools.nexus.secure_tcp_socket.dto.Message;

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

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // create client and run it
        var client = new ExampleClient(SERVER, PORT);
        client.connectToServer();

        // use connection
        client.getOutput().writeObject(Message.createListRequest());
        Message message = (Message) client.getInput().readUnshared();
        log(message.command + " message received: " + message.name);
    }

    static void log(String str) {
        System.out.println(str);
    }
}
