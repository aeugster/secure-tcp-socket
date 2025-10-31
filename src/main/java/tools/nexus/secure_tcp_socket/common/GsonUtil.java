package tools.nexus.secure_tcp_socket.common;

import java.util.List;

/**
 * The gson library converts simple byte arrays to ArrayList of Doubles...
 * This lib is either "bad" OR "can't guess the right type"
 * <p>
 * Bad:
 * - Presence of solutions like <a href="https://gist.github.com/orip/3635246">this class</a> might have a reason.
 * <p>
 * Can't guess:
 * - Deserialize a field of type object is difficult.
 */
class GsonUtil {

    private GsonUtil() {
    }

    public static byte[] objectToByteArray(Object obj) {
        List<Double> givenList = (List<Double>) obj;
        var result = new byte[givenList.size()];

        for (int i = 0; i < givenList.size(); i++) {
            result[i] = givenList.get(i).byteValue();
        }

        return result;
    }
}
