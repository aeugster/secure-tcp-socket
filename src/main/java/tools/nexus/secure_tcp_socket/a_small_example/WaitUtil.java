package tools.nexus.secure_tcp_socket.a_small_example;

import java.util.function.BooleanSupplier;

public class WaitUtil {

    private WaitUtil() {
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
