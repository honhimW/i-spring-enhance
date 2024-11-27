package io.github.honhimw.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class Base64UtilsTest {


    @Test
    void encode() {
        Assertions.assertEquals("Zm9v", Base64Utils.encode("foo"));
    }

    @Test
    void testEncode() {
        Assertions.assertEquals("Pz8=", Base64Utils.encode("中文", StandardCharsets.ISO_8859_1));
        Assertions.assertEquals("5Lit5paH", Base64Utils.encode("中文", StandardCharsets.UTF_8));
    }

    @Test
    void decode() {
        Assertions.assertEquals("foo", Base64Utils.decode("Zm9v"));
    }

    @Test
    void testDecode() {
        Assertions.assertEquals("中文", Base64Utils.decode("Pz8=", StandardCharsets.ISO_8859_1));
        Assertions.assertEquals("中文", Base64Utils.decode("5Lit5paH", StandardCharsets.UTF_8));
    }
}