package tools.nexus.secure_tcp_socket.a_small_example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.nexus.secure_tcp_socket.FortNoxServer;
import tools.nexus.secure_tcp_socket.common.ObjInputStream;
import tools.nexus.secure_tcp_socket.common.ObjOutputStream;
import tools.nexus.secure_tcp_socket.common.SyncObjOutputStream;
import tools.nexus.secure_tcp_socket.dto.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Expects a fixed serie of messages (because it's an example server)
 */
@Slf4j
@RequiredArgsConstructor
public class ExampleServer {

    private static boolean testFlagDidListen;

    private final ServerSocket serverSocket;

    private static boolean doWhile = true;

    /**
     * use for manual start
     */
    @SuppressWarnings({"java:S2189", "java:S4507"}) // no infinite loops, no stack trace
    public static void main(String[] args) throws IOException {
        var port = SecSocketExample.PROD_PORT;
        var once = false;

        if (args != null) {
            if (args.length > 0 && !args[0].equals("p")) {
                port = Integer.parseInt(args[0]);
            }
            if (args.length > 1 && args[1].equals("once")) {
                once = true;
            }
        }

        var serverSocket = new ServerSocket(port);

        ExampleServer server = new ExampleServer(serverSocket);
        ExampleServer.testFlagDidListen = true;

        while (doWhile) {
            if (once) {
                doWhile = false;
            }

            try {
                server.runServer();
            } catch (Exception e) {
                server.log("Restarting server loop because of: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static boolean isTestFlagDidListen() {
        return testFlagDidListen;
    }

    void runServer() throws IOException {
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
    private void acceptAndSecureConnection(ServerSocket serverSocket, FortNoxServer fnServer) throws IOException {
        try (Socket connectedClient = fnServer.createSecureSocketViaIdentifierRemoveKey(serverSocket.accept())) {

            // drop clients 'list'
            Message message = (Message) new ObjInputStream(connectedClient.getInputStream()).readUnshared();
            log("Server reacting to: " + message.command);

            Message m = Message.createListRequest();
            m.obj = new HashMap<>();
            ((Map) m.obj).put("helloWorld.txt", "file1");

            new ObjOutputStream(connectedClient.getOutputStream()).writeUnshared(m);
            log(m.command + " message sent");
        }
    }

    // no std out
    @SuppressWarnings("java:S106")
    void log(String str) {
        System.out.println(str);
    }
}
