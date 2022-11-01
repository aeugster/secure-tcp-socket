package tools.nexus.secure_tcp_socket.a_small_example;

import tools.nexus.secure_tcp_socket.FortNoxClient;
import tools.nexus.secure_tcp_socket.common.ObjInputStream;
import tools.nexus.secure_tcp_socket.common.SecureTcpSocket;
import tools.nexus.secure_tcp_socket.common.SyncObjOutputStream;

import java.io.IOException;
import java.net.Socket;

/**
 * Example client
 */
public class ExampleClient {

    public final String server;
    public final int port;

    private SecureTcpSocket secureTcpSocket;
    private SyncObjOutputStream output;
    private ObjInputStream input;

    public ExampleClient(String server, int port) {
        this.server = server;
        this.port = port;
    }

    public void connectToServer() throws IOException {
        SecureTcpSocket secureSocket;
        try (var connectedServer = new Socket(server, port)) {

            /*
             * use ForNoxClient to retrieve secure connection
             */
            var fnClient = new FortNoxClient();
            secureSocket = fnClient.action2setupSecureSocket(server, connectedServer, new SyncObjOutputStream(connectedServer.getOutputStream()));
        }

        secureTcpSocket = secureSocket;
    }

    public SyncObjOutputStream getOutput() throws IOException {
        if (output == null) {
            output = new SyncObjOutputStream(secureTcpSocket.getOutputStream());
        }
        return output;
    }

    public ObjInputStream getInput() throws IOException {
        if (input == null) {
            input = new ObjInputStream(secureTcpSocket.getInputStream());
        }
        return input;
    }
}
