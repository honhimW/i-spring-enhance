package io.github.honhimw.test;

import io.github.honhimw.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2025-03-25
 */

public class JsonUtilTests {

    @Test
    @SneakyThrows
    void flatten() {
        Foo instance = Foo.instance();
        Map<String, Object> flatten = JsonUtils.flatten(instance);
        flatten.forEach((s, o) -> System.out.println(s + ": " + o));
        assert flatten.containsKey("/bar");
        assert flatten.containsKey("/duration");
        assert flatten.containsKey("/next/bar");
        assert flatten.containsKey("/next/duration");
        assert !flatten.containsKey("/next/next");
        assert !flatten.containsKey("/next/props");
        assert flatten.containsKey("/props/0");
        assert flatten.containsKey("/props/1");
        assert flatten.containsKey("/props/2");
        assert flatten.containsKey("/props/3");
        assert flatten.containsKey("/props/4");

    }

    @Getter
    @Setter
    public static class Foo {
        private String bar;
        private Duration duration;
        private Foo next;
        private List<Object> props;

        public static Foo instance() {
            Foo foo = new Foo();
            foo.setBar("bar");
            foo.setDuration(Duration.ofSeconds(1));
            Foo next = new Foo();
            next.setBar("bar2");
            next.setDuration(Duration.ofSeconds(10_000_000));
            foo.setNext(next);
            foo.setProps(List.of("foo", "bar", 1, false, 3.2));
            return foo;
        }

    }

}
