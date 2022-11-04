package tools.nexus.secure_tcp_socket.common;

import java.util.function.BooleanSupplier;

/**
 * Please use with care or for example code
 */
public class DeprecatedWaitUtil {

    private DeprecatedWaitUtil() {
    }

    public static void waitMillis(final int millis, BooleanSupplier waitFor) {
        for (int i = 0; i < millis; i++) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (waitFor.getAsBoolean()) {
                break;
            }
        }
    }
}
