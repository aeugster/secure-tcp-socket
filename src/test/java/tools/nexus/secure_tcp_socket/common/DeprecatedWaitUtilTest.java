package tools.nexus.secure_tcp_socket.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class DeprecatedWaitUtilTest {

    @Test
    @Timeout(1)
    @SuppressWarnings("java:S2699") // no assertions
    void testMaximumReturn() {
        DeprecatedWaitUtil.waitMillis(250, () -> false);
    }

    @Test
    @Timeout(1)
    @SuppressWarnings("java:S2699") // no assertions
    void testBreakReturn() {
        DeprecatedWaitUtil.waitMillis(2048, () -> true);
    }

}