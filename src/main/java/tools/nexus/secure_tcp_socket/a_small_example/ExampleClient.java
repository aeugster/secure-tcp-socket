package tools.nexus.secure_tcp_socket.a_small_example;

import lombok.RequiredArgsConstructor;
import tools.nexus.secure_tcp_socket.FortNoxClient;
import tools.nexus.secure_tcp_socket.common.ObjInputStream;
import tools.nexus.secure_tcp_socket.common.SecureTcpSocket;
import tools.nexus.secure_tcp_socket.common.SyncObjOutputStream;
import tools.nexus.secure_tcp_socket.exceptions.SecureSocketTechnicalException;

import java.io.IOException;
import java.net.Socket;

/**
 * Example client
 */
@RequiredArgsConstructor
public class ExampleClient {

    private final String server;
    private final int port;
    private final byte firstByte;

    private SecureTcpSocket secureTcpSocket;
    private SyncObjOutputStream output;
    private ObjInputStream input;

    public void connectToServer() throws IOException {
        SecureTcpSocket secureSocket;
        try (var connectedServer = new Socket(server, port)) {

            /*
             * use ForNoxClient to retrieve secure connection
             */
            var fnClient = new FortNoxClient();
            secureSocket = fnClient.action2setupSecureSocket(server, connectedServer, new SyncObjOutputStream(connectedServer.getOutputStream()));

            /*
             * compare provided key with (in your java code stored) expected key
             * (please compare all bytes for productive code)
             */
            byte currentFirstByte = fnClient.getPublicKeyBytes()[0];
            if (currentFirstByte != firstByte) {
                throw new SecureSocketTechnicalException("Given key is " + currentFirstByte + " but it should be: " + firstByte);
            }
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
