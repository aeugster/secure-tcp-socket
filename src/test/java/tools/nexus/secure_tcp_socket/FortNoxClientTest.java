package tools.nexus.secure_tcp_socket;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.nexus.secure_tcp_socket.common.SyncObjOutputStream;

class FortNoxClientTest {

    /**
     * high-level test, the system tests do better coverage
     */
    @Test
    void testSetupSecureSocket1() {

        // arrange
        SyncObjOutputStream trans = Mockito.mock(SyncObjOutputStream.class);

        // act
        try {
            new FortNoxClient().action2setupSecureSocket(null, null, trans);
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("could not setup"));
            return;
        }

        Assertions.fail();
    }

}
