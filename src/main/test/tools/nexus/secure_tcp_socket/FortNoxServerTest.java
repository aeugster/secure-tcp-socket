package tools.nexus.secure_tcp_socket;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

class FortNoxServerTest {

    final static String REF = "Das ist das Haus vom Nikolaus.";

    @Test
    void testGenerateSymmetricKey() throws NoSuchAlgorithmException {
        Key symKey = FortNoxClient.generateSymmetricKey(FortNoxClient.SYMMETRIC_TYPE, FortNoxClient.SYMMETRIC_KEY_SIZE);
        Assertions.assertEquals("AES", symKey.getAlgorithm());
        Assertions.assertEquals(16 /* 128 */, symKey.getEncoded().length);
    }

    @Test
    void testFullAsymmetric() throws Exception {
        KeyPair keyPair = FortNoxServer.generateKeyPair("RSA", FortNoxServer.ASYM_KEY_SIZE);

        byte[] cypherText = FortNoxClient.encrypt(REF.getBytes(StandardCharsets.UTF_8), keyPair.getPublic());
        byte[] clearBytes = FortNoxServer.decrypt(cypherText, keyPair.getPrivate());

        String backToString = new String(clearBytes, StandardCharsets.UTF_8);
        assertThat(backToString).isEqualTo(REF);
    }

}
