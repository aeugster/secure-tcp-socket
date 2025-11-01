package tools.nexus.secure_tcp_socket.dto;

import java.io.Serializable;
import java.util.Arrays;

@SuppressWarnings("squid:ClassVariableVisibilityCheck")
//@EqualsAndHashCode
public class Message implements Serializable {

	// 01: 24. Oct 2022 - migrated from private source to open source
	private static final long serialVersionUID = 1L;

	public String command;

	/**
	 * Usually the name of the file<br/>
	 * - put: file name<br/>
	 * - get: id of file<br/>
	 * - txt: message to send
	 */
	public String name;

	/**
	 * The file (tunnel data)
	 */
	public byte[] buffer;

	/**
	 * - list: map<string, file><br>
	 * - putPubK: public key<br>
	 * - putEncSymKey: the initVector as byte[] (IvParamSpec does not serialize)
	 * - put: lastModified as long<br>
	 */
	@SuppressWarnings("squid:S1948")
	// not serializable
	public Object obj;

	/**
	 * Tunneling stuff
	 */
	public int connID;
	public int targetPort;

	/**
	 * Constructor
	 */
	public Message(String cmd) {
		this.command = cmd;
	}

	public static Message createListRequest() {
		return new Message(SecSocketMessageCmd.list);
	}

	public boolean isListRequest() {
		return SecSocketMessageCmd.list.equals(command);
	}

	/**
	 * Hash with all fields (except storedHash)
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(buffer);
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		result = prime * result + connID;
		result = prime * result + ((name == null) ? 0 : name.hashCode());

		if (obj instanceof byte[]) {
			result = prime * result + Arrays.hashCode((byte[]) obj);
		} else {
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		}

		result = prime * result + targetPort;
		return result;
	}

	/**
	 * Equals with all fields (except storedHash)
	 */
	@SuppressWarnings({"squid:S3776", "squid:S1126", "squid:S3973"})
	// Cognitive Complexity, return boolean not with if-else, use curly braces or indentation
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (!Arrays.equals(buffer, other.buffer))
			return false;
		if (command == null) {
			if (other.command != null)
				return false;
		} else if (!command.equals(other.command))
			return false;
		if (connID != other.connID)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj))
			return false;
		if (targetPort != other.targetPort)
			return false;
		return true;
	}

	/**
	 * The stored hash code
	 */
	public int storedHash;
}
