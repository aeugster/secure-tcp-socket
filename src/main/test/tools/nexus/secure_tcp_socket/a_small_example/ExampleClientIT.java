package tools.nexus.secure_tcp_socket.a_small_example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.nexus.secure_tcp_socket.common.ObjInputStream;
import tools.nexus.secure_tcp_socket.common.SecureTcpSocket;
import tools.nexus.secure_tcp_socket.common.SyncObjOutputStream;
import tools.nexus.secure_tcp_socket.dto.Message;

import java.io.IOException;
import java.util.Map;

import static tools.nexus.secure_tcp_socket.a_small_example.SecSockeExample.PORT;

/**
 * integration test against reference server
 */
class ExampleClientIT {

    private static final String SERVER_INTEGRATION = "nexus.tools";

    /**
     * receive of listing
     * <p>
     * Note:
     * - delete line where storedHash is updated and see how the server reacts
     */
    @Test
    void testReceiveOfListing() throws IOException, ClassNotFoundException {

        // run client
        var client = new ExampleClient(SERVER_INTEGRATION, PORT);
        SecureTcpSocket secureTcpSocket = client.run();

        // send message
        var request = Message.getListRequest();
        request.storedHash = request.hashCode();
        new SyncObjOutputStream(secureTcpSocket.getOutputStream()).writeObject(request);

        // receive
        var response = (Message) new ObjInputStream(secureTcpSocket.getInputStream()).readUnshared();

        // assert
        Assertions.assertNull(response.name, "An error occurs e.g. if storeHash is not set");
        var listing = (Map<String, String>) response.obj;

        Assertions.assertTrue(listing.size() > 0, "Currently no files on the Server");
    }

}