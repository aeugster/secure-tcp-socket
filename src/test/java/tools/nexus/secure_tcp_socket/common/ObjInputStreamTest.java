package tools.nexus.secure_tcp_socket.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.nexus.secure_tcp_socket.dto.Message;
import tools.nexus.secure_tcp_socket.exceptions.ConfigFacade;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ObjInputStreamTest {

    ObjInputStream testee;

    ByteArrayInputStream bais;

    @BeforeEach
    void setup() throws IOException {
        byte[] arr = ("[{\"command\":\"putEncSymKey\",\"name\":\"Hello fromJson\",\"obj\":[109,121,65,114,114,97,121],\"connID\":0,\"targetPort\":0,\"storedHash\":0}" + "," +
                "{\"command\":\"list\",\"name\":\"blu456\",\"connID\":0,\"targetPort\":0,\"storedHash\":0}]").getBytes(StandardCharsets.UTF_8);
        bais = new ByteArrayInputStream(arr);
        testee = new ObjInputStream(bais);
    }

    @Test
    void testReadMessage() {

        // assert I (magic string: putEncSymKey)
        var message = (Message) testee.readUnshared();
        assertThat(message.name).isEqualTo("Hello fromJson");
        assertThat((byte[]) message.obj).isEqualTo("myArray".getBytes(StandardCharsets.UTF_8));

        // assert II
        message = (Message) testee.readUnshared();
        assertThat(message.name).isEqualTo("blu456");
    }

    @Test
    void testJsonRestoreHandler() {
        var m = new Message("xy");
        m.obj = "123";
        ConfigFacade.registerJsonHandler((obj, cmd) -> 456);

        testee.restoreMessage(m);

        assertThat((Integer) m.obj).isEqualTo(456);
    }
}