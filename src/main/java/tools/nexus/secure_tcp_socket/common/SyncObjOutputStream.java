package tools.nexus.secure_tcp_socket.common;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Object adapter to make writing to clients thread-safe
 */
public class SyncObjOutputStream {

	private final ObjOutputStream stream;

	public SyncObjOutputStream(OutputStream os) throws IOException {
		stream = new ObjOutputStream(os);
	}

	/**
	 * Does an unshared write
	 */
	public synchronized void writeObject(Object obj) throws IOException {
		stream.writeUnshared(obj);
	}

	public synchronized void flush() throws IOException {
		stream.flush();
	}
}
