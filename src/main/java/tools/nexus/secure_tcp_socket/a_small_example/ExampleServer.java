package tools.nexus.secure_tcp_socket.a_small_example;

import tools.nexus.secure_tcp_socket.FortNoxServer;
import tools.nexus.secure_tcp_socket.common.ObjInputStream;
import tools.nexus.secure_tcp_socket.common.ObjOutputStream;
import tools.nexus.secure_tcp_socket.common.SyncObjOutputStream;
import tools.nexus.secure_tcp_socket.dto.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Expects a fixed serie of messages (because it's an example server)
 */
public class ExampleServer {

    private final ServerSocket serverSocket;

    public ExampleServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @SuppressWarnings("java:S2189") // infinite loops
    public static void main(String[] args) throws IOException {
        var server = new ExampleServer(new ServerSocket(SecSockeExample.PORT));

        while (true) {
            try {
                server.runServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void runServer() throws IOException, ClassNotFoundException {
        log("\nWaiting for new client...");

        Socket connectedClient = serverSocket.accept();
        log("client connected");

        // unencrypted streams
        var input = new ObjInputStream(connectedClient.getInputStream());
        var outputStream = new SyncObjOutputStream(connectedClient.getOutputStream());

        var fnServer = new FortNoxServer();

        // drop clients 'getPubK'
        Message message = (Message) input.readUnshared();
        log("Server reacting to: " + message.command);
        fnServer.action1sendPublicKey(outputStream, "keypair.bin");
        log("sent pub key");

        // handle sym key
        message = (Message) input.readUnshared();
        fnServer.action3asymmetricDecryptKey(message.buffer, outputStream, connectedClient, (byte[]) message.obj);
        log("symmetric key has been stored");

        acceptAndSecureConnection(serverSocket, fnServer);
        log("ending server cycle");
    }

    @SuppressWarnings("java:S3329") // IV is dynamical
    private void acceptAndSecureConnection(ServerSocket serverSocket, FortNoxServer fnServer) throws IOException, ClassNotFoundException {
        Socket connectedClient = fnServer.createSecureSocketViaIdentifier(serverSocket.accept());

        // drop clients 'list'
        Message message = (Message) new ObjInputStream(connectedClient.getInputStream()).readUnshared();
        log("Server reacting to: " + message.command);

        Message m = Message.createListRequest();
        m.name = "helloWorld.txt, helloWorld.png, helloWorld.jpg";
        new ObjOutputStream(connectedClient.getOutputStream()).writeUnshared(m);
        log(m.command + " message sent");
    }

    void log(String str) {
        System.out.println(str);
    }
}
