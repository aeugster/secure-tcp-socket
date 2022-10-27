package tools.nexus.secure_tcp_socket.a_small_example;

import java.io.IOException;

/**
 * Example with client / server
 */
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

        System.out.println("client and writeHelloWorld: Ended");
    }
}
