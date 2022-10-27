package tools.nexus.secure_tcp_socket.a_small_example;

import tools.nexus.secure_tcp_socket.FortNoxClient;
import tools.nexus.secure_tcp_socket.SecureTcpSocket;
import tools.nexus.secure_tcp_socket.common.SyncObjOutputStream;

import java.io.IOException;
import java.net.Socket;

/**
 * Example client
 */
public class ExampleClient {

    public final String server;
    public final int port;

    public ExampleClient(String server, int port) {
        this.server = server;
        this.port = port;
    }

    public SecureTcpSocket run() throws IOException {
        SecureTcpSocket secureSocket;
        try (var connectedServer = new Socket(server, port)) {

            var fnClient = new FortNoxClient();
            secureSocket = fnClient.setupSecureSocket2(server, connectedServer, new SyncObjOutputStream(connectedServer.getOutputStream()));
        }

        return secureSocket;
    }
}
