package tools.nexus.secure_tcp_socket.a_small_example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.nexus.secure_tcp_socket.exceptions.SecureSocketTechnicalException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OK, does work locally
 * OK, does crash locally if port used: "Address already in use: NET_Bind
 * tbd, on github.com
 */
class SecSockeExampleTest {

    @Test
    void testSuccessfulEnding() throws IOException, ClassNotFoundException, InterruptedException {

        // act
        SecSockeExample.main(null);

        // assert listen
        assertThat(ExampleServer.isTestFlagDidListen()).isTrue();

        // assert receive
        var message = SecSockeExample.getTestFlagReceivedMessage();
        assertThat(message.command).isEqualTo("list");
    }

    @Test
    void coverageInvalid() {
        Assertions.assertThrows(SecureSocketTechnicalException.class,
                () -> SecSockeExample.runServer(new String[]{"-3"}));
    }
}