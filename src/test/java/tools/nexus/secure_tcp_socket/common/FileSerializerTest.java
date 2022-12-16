package tools.nexus.secure_tcp_socket.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileSerializerTest {

    private static final String text = "Hello World";

    FileSerializer<String> testee = new FileSerializer<>("target/jUnit/serializedString.txt");

    @Test
    void testWriteObjGetObj() {
        testee.writeObj(text);

        var actual = testee.getObj();
        assertThat(actual).isEqualTo(text);
    }

}