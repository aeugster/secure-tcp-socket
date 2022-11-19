package tools.nexus.secure_tcp_socket.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.nexus.secure_tcp_socket.dto.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ObjOutputStreamTest {

    ObjOutputStream testee;

    ByteArrayOutputStream baos;

    @BeforeEach
    void setup() throws IOException {
        baos = new ByteArrayOutputStream(100);
        testee = new ObjOutputStream(baos);
    }

    @Test
    void testToJsonAndPipelining() throws IOException {
        var message = Message.createListRequest();
        message.name = "Hello toJson";

        testee.writeUnshared(message);

        message.name = "bla123";
        testee.writeUnshared(message);

        // assert
        assertThat(baos.toString()).contains("\"name\":\"Hello toJson\"");
        assertThat(baos.toString()).contains("\"name\":\"bla123\"");
    }
}