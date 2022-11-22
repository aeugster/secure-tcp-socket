package tools.nexus.secure_tcp_socket.common;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import tools.nexus.secure_tcp_socket.dto.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class ObjOutputStream {

    JsonWriter jsonWriter;

    public ObjOutputStream(OutputStream outputStream) throws IOException {
        jsonWriter = new JsonWriter(new OutputStreamWriter(outputStream));

        // pipelining
        jsonWriter.beginArray();
    }

    public void writeUnshared(Object obj) throws IOException {
        var gson = new Gson();
        gson.toJson(obj, Message.class, jsonWriter);

        // without, it's pretty passive
        jsonWriter.flush();
    }

    public void flush() throws IOException {
        jsonWriter.flush();
    }

    public void close() throws IOException {
        jsonWriter.close();
    }
}
