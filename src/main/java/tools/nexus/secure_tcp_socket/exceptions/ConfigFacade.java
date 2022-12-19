package tools.nexus.secure_tcp_socket.exceptions;

import tools.nexus.secure_tcp_socket.common.ObjInputStream;

import java.util.function.BiFunction;

public class ConfigFacade {

    public static void registerJsonHandler(BiFunction<Object, String, Object> handler) {
        ObjInputStream.registerJsonHandler(handler);
    }
}
