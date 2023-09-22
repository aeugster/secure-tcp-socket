package tools.nexus.secure_tcp_socket.dto;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTest {

    Message testee = new Message("tbd");

    Message otherMessage = new Message("tbd");
    Message nonEqMessage = new Message("nonEq");

    @Test
    void testHashCode_detectMessageChanges() {
        assertThat(testee.hashCode()).isEqualTo(-638242825);

        testee.name = "hello";
        assertThat(testee.hashCode()).isEqualTo(167468105);

        testee.buffer = "bye".getBytes(StandardCharsets.UTF_8);
        assertThat(testee.hashCode()).isEqualTo(262041884);
    }

    @Test
    @SuppressWarnings("java:S5853") // chain asserts
    void testEqual() {
        assertThat(testee).isEqualTo(otherMessage);
        assertThat(testee).isNotEqualTo(nonEqMessage);
    }
}
