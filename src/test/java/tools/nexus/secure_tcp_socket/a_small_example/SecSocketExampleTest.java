package tools.nexus.secure_tcp_socket.a_small_example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.nexus.secure_tcp_socket.exceptions.SecureSocketTechnicalException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * - starts the {@link ExampleServer}
 * - tests whether server made it to state listen
 * - validates client-received message
 * <p>
 * OK, does work locally
 * OK, does crash locally if port used: "Address already in use: NET_Bind
 */
class SecSocketExampleTest {

    @Test
    void testSuccessfulEnding() throws IOException, ClassNotFoundException {

        // act
        SecSocketExample.main(new String[]{"8234", "once"});

        // assert listen
        assertThat(ExampleServer.isTestFlagDidListen()).isTrue();

        // assert receive
        var message = SecSocketExample.getTestFlagReceivedMessage();
        assertThat(message.command).isEqualTo("list");
    }

    @Test
    void coverageInvalidArguments() {
        Assertions.assertThrows(SecureSocketTechnicalException.class,
                () -> SecSocketExample.runServer(new String[]{"-3"}));
    }
}