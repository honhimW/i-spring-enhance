package io.github.honhimw.util;

import lombok.SneakyThrows;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

/**
 * @author honhimW
 * @since 2025-05-21
 */

public class BytesTests {

    @Test
    @SneakyThrows
    void fromObject() {
        SomeObject object = new SomeObject();
        object.name = "foo";
        object.age = 123;
        Bytes bytes = Bytes.fromObject(object);
        System.out.println(bytes.asBase64());
        System.out.println(bytes.<Object>asObject());
    }

    @ToString
    public static class SomeObject implements Serializable {
        String name;
        Integer age;
    }

}
