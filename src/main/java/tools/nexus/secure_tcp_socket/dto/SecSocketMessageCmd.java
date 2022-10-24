package tools.nexus.secure_tcp_socket.dto;

@SuppressWarnings("all")
public interface SecSocketMessageCmd {

    /**
     * key exchange
     */
    String getPubK = "getPubK";
    String putPubK = "putPubK";
    String putEncSymKey = "putEncSymKey";
    String ready = "ready";

    /**
     * possible command for listing things
     */
    String list = "list";

}
