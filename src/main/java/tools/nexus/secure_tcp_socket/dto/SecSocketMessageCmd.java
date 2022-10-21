package tools.nexus.secure_tcp_socket.dto;
// FIXME remove unused commands
/**
 * Supported message commands - To avoid further issues this is now an interface<br>
 * - also see HiddenFlags
 */
@SuppressWarnings("all")
public interface SecSocketMessageCmd {

	/**
	 * Encryption
	 */
	String getPubK = "getPubK";
	String putPubK = "putPubK";
	String putEncSymKey = "putEncSymKey";
	String ready = "ready";

	/**
	 * File operations
	 */
	String put = "put";
	String get = "get";
	String publish = "publish"; // changes working folder to www/<user>
	String list = "list";
	String delete = "delete";
	String cd = "cd"; // change directory

	/**
	 * Security
	 */
	String login = "login";
	String logout = "logout";
	String rld = "rld";
	String register = "register";

	/**
	 * Chat messages (sending string would be version-independent, but errors are not subject of celebration)
	 */
	String txt = "txt";

	/**
	 * Tunnelhandler
	 */
	String create = "create";
	String data = "data";
	String destroy = "destroy";

	/**
	 * Others
	 */
	String quit = "quit";
	String rev = "rev";
}
