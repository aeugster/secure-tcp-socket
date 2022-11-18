package tools.nexus.secure_tcp_socket.common;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class ObjInputStream extends ObjectInputStream {

    private final Set<String> allowedClasses;

    public ObjInputStream(InputStream inputStream) throws IOException {
        super(inputStream);

        allowedClasses = new HashSet<>();
        allowedClasses.add("tools.nexus.secure_tcp_socket.dto.Message");

        // ExampleClientIT
        allowedClasses.add("java.security.KeyRep");
        allowedClasses.add("[B");
        allowedClasses.add("java.security.KeyRep$Type");
        allowedClasses.add("java.lang.Enum");
        allowedClasses.add("java.util.HashMap");

        // file transfer
        allowedClasses.add("java.lang.Long");
        allowedClasses.add("java.lang.Number");
    }

    /**
     * Deserialization should not be vulnerable to injection attacks
     */
    @Override
    protected Class<?> resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {
        // Only deserialize instances of AllowedClass
        if (!allowedClasses.contains(osc.getName())) {
            throw new InvalidClassException("Unauthorized deserialization", osc.getName());
        }
        return super.resolveClass(osc);
    }

}
