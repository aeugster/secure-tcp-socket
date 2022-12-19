package tools.nexus.secure_tcp_socket.common;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import tools.nexus.secure_tcp_socket.dto.Message;
import tools.nexus.secure_tcp_socket.dto.SecSocketMessageCmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

public class ObjInputStream {

    private static BiFunction<Object, String, Object> jsonHandler = (obj, cmd) -> null;

    private final Gson gson;
    private final JsonReader jsonReader;

    public ObjInputStream(InputStream inputStream) throws IOException {
        gson = new Gson();

        jsonReader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        jsonReader.beginArray();
    }

    public static void registerJsonHandler(BiFunction<Object, String, Object> handler) {
        jsonHandler = handler;
    }

    public Object readUnshared() {
        var parsedMessage = (Message) gson.fromJson(jsonReader, Message.class);
        restoreMessage(parsedMessage);

        return parsedMessage;
    }

    public void close() throws IOException {
        jsonReader.close();
    }

    void restoreMessage(Message message) {
        var cmd = message.command;

        Object newObject = jsonHandler.apply(message.obj, message.command);
        if (newObject != null) {
            message.obj = newObject;
            return;
        }

        if (cmd.equals(SecSocketMessageCmd.putEncSymKey) || cmd.equals(SecSocketMessageCmd.putPubK)) {
            message.obj = GsonUtil.objectToByteArray(message.obj);
        }
    }
}
