package tools.nexus.secure_tcp_socket.a_small_example;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Example with client / server
 * <p>
 * - Remove <scope>test</scope> in pom.xml to have comments printed
 */
@Slf4j
public class SecSockeExample {

    public static final int PORT = 1234;
    public static final String SERVER = "nexus.tools";

    public static void main(String[] args) throws IOException {

        // TODO: create SERVER and start it
        // var server = new ExampleServer(PORT)
        // new Thread(server::runServer).start()

        // create client and run it
        var client = new ExampleClient(SERVER, PORT);
        client.run();

        log.info("Client thread: Ended");
    }
}
