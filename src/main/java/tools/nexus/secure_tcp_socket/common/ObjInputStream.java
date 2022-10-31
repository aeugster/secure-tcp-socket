package tools.nexus.secure_tcp_socket.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class ObjInputStream {

    ObjectInputStream input;

    public ObjInputStream(InputStream inputStream) throws IOException {
        input = new ObjectInputStream(inputStream);
    }

    public Object readUnshared() throws ClassNotFoundException, IOException {
        return input.readUnshared();
    }

    public void close() throws IOException {
        input.close();
    }

}
