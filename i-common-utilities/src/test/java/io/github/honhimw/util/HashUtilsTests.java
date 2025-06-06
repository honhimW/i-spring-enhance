package io.github.honhimw.util;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * @author honhimW
 * @since 2025-06-06
 */

public class HashUtilsTests {

    @Test
    @SneakyThrows
    void print() {
        String[] algorithms = {
            HashUtils.MD2,
            HashUtils.MD5,
            HashUtils.SHA1,
            HashUtils.SHA224,
            HashUtils.SHA256,
            HashUtils.SHA384,
            HashUtils.SHA512
        };

        HashUtils hashUtils = HashUtils.newInstance(algorithms);
        HashUtils update = hashUtils.update("foo bar".getBytes());

        for (String algorithm : algorithms) {
            String hexString = update.toHexString(algorithm);
            int length = hexString.length();
            String key = StringUtils.rightPad("%s(%d)".formatted(algorithm, length), 12);
            System.out.println(key + ": " + hexString);
        }

    }

}
