package tools.nexus.secure_tcp_socket.a_small_example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecSocketArgumentsTest {

    @Test
    void testNull() {
        var result = SecSocketArguments.parse(null);

        assertThat(result).isNotNull();
    }

    @Test
    void testOfTheTrack() {
        String[] args = {"p", "multi"};
        var result = SecSocketArguments.parse(args);

        assertThat(result.isOnce()).isFalse();
    }
}