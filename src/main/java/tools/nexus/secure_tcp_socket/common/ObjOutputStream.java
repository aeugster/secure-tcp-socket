package tools.nexus.secure_tcp_socket.common;

import tools.nexus.secure_tcp_socket.exceptions.SecureSocketApplicationException;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ObjOutputStream {

    ObjectOutputStream output;

    public ObjOutputStream(OutputStream outputStream) throws IOException {
        output = new ObjectOutputStream(outputStream);
    }

    public void writeObject(Object obj) throws SecureSocketApplicationException {
        throw new SecureSocketApplicationException("Don't use writeObject");
    }

    public void writeUnshared(Object obj) throws IOException {
        output.writeUnshared(obj);
        output.reset();
    }

    public void flush() throws IOException {
        output.flush();
    }

    public void close() throws IOException {
        output.close();
    }
}
