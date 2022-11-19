package tools.nexus.secure_tcp_socket.a_small_example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import tools.nexus.secure_tcp_socket.dto.Message;

import java.io.IOException;
import java.util.Map;

/**
 * integration test against reference server
 */
class ExampleClientIT {

    private static final String PROD_SERVER = "nexus.tools";
    // private static final String PROD_SERVER = "localhost";

    /**
     * receive of listing
     * <p>
     * Note:
     * - delete line where storedHash is updated and see how the server reacts
     */
    @Test
    @Timeout(3)
    void testReceiveOfListing() throws IOException {

        // run client
        var client = new ExampleClient(PROD_SERVER, SecSocketExample.PROD_PORT, SecSocketExample.PROD_FIRST_PUBLIC_KEY_BYTE);
        client.connectToServer();

        // send message
        var request = Message.createListRequest();
        request.storedHash = request.hashCode();
        client.getOutput().writeObject(request);

        // receive
        var response = (Message) client.getInput().readUnshared();

        // assert
        Assertions.assertNull(response.name, "An error occurs e.g. if storeHash is not set");
        var listing = (Map<String, String>) response.obj;

        Assertions.assertTrue(listing.size() > 0, "Currently no files on the Server");
    }

}
