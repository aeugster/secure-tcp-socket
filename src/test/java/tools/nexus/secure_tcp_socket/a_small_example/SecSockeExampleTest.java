package tools.nexus.secure_tcp_socket.a_small_example;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class SecSockeExampleTest {

    @Test
    void testSuccessfulEnding() throws IOException, ClassNotFoundException {

        // act
        SecSockeExample.main(null);

        // assert listen
        assertThat(ExampleServer.isTestFlagDidListen()).isTrue();

        // assert receive
        var message = SecSockeExample.getTestFlagReceivedMessage();
        assertThat(message.command).isEqualTo("list");
    }

}