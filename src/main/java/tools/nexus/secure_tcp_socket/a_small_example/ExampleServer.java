package tools.nexus.secure_tcp_socket.a_small_example;

import tools.nexus.secure_tcp_socket.FortNoxClient;
import tools.nexus.secure_tcp_socket.FortNoxServer;
import tools.nexus.secure_tcp_socket.common.ObjInputStream;
import tools.nexus.secure_tcp_socket.common.ObjOutputStream;
import tools.nexus.secure_tcp_socket.common.SecureTcpSocket;
import tools.nexus.secure_tcp_socket.common.SyncObjOutputStream;
import tools.nexus.secure_tcp_socket.dto.Message;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Expects a fixed serie of messages (because it's an example server)
 */
public class ExampleServer {

    private static final Map<String, Object[]> HOLY_MAP_OF_KEYS = new HashMap<>();

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
        fnServer.action3asymmetricDecryptKey(message.buffer, outputStream, connectedClient, HOLY_MAP_OF_KEYS, (byte[]) message.obj);
        log("symmetric key has been stored");

        acceptSecuredConnection(serverSocket);
        log("ending server cycle");
    }

    @SuppressWarnings("java:S3329") // IV is dynamical
    private void acceptSecuredConnection(ServerSocket serverSocket) throws IOException, ClassNotFoundException {
        String firstKey = (String) HOLY_MAP_OF_KEYS.keySet().toArray()[0];

        Socket connectedClient = SecureTcpSocket.of(serverSocket.accept(), FortNoxClient.SYMMETRIC_ALGORITHM,
                (SecretKey) HOLY_MAP_OF_KEYS.get(firstKey)[0],
                new IvParameterSpec((byte[]) HOLY_MAP_OF_KEYS.get(firstKey)[1]));

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
