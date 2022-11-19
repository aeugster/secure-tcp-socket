package tools.nexus.secure_tcp_socket.common;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import tools.nexus.secure_tcp_socket.dto.Message;
import tools.nexus.secure_tcp_socket.dto.SecSocketMessageCmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ObjInputStream {

    private final Gson gson;
    private final JsonReader jsonReader;

    public ObjInputStream(InputStream inputStream) throws IOException {
        gson = new Gson();

        jsonReader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        jsonReader.beginArray();
    }

    public Object readUnshared() {
        var changedMessage = (Message) gson.fromJson(jsonReader, Message.class);
        return restoreMessage(changedMessage);
    }

    public void close() throws IOException {
        jsonReader.close();
    }

    private Object restoreMessage(Message message) {
        var cmd = message.command;

        if (cmd.equals(SecSocketMessageCmd.putEncSymKey) || cmd.equals(SecSocketMessageCmd.putPubK)) {
            message.obj = GsonUtil.objectToByteArray(message.obj);
        }

        return message;
    }
}
