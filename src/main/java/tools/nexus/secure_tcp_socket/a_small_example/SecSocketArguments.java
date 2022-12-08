package tools.nexus.secure_tcp_socket.a_small_example;

import lombok.Getter;

/**
 * Use the followings args to start:
 * [0] port-number or "p" for default one
 * [1] "once" for termination after one client
 */
@Getter
public class SecSocketArguments {

    private int port = SecSocketExample.PROD_PORT;
    private boolean once = false;

    public static SecSocketArguments parse(String[] args) {

        var result = new SecSocketArguments();

        if (args == null) {
            return result;
        }

        if (args.length > 0 && !args[0].equals("p")) {
            result.port = Integer.parseInt(args[0]);
        }
        if (args.length > 1 && args[1].equals("once")) {
            result.once = true;
        }

        return result;
    }
}
