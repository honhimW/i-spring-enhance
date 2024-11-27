package io.github.honhimw.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author hon_him
 * @since 2024-11-26
 */

@Slf4j
public class ThreadUtilsTest {

    @Test
    @SneakyThrows
    void block() {
        List<Integer> list = IntStream.range(0, 100).boxed().toList();
        ThreadUtils.block(10, list, integer -> {
            log.info("{}", integer);
            try {
                Thread.sleep(new Random().nextInt(10, 100));
            } catch (Exception ignored) {
            }
            return Stream.of(integer.toString());
        });
    }

    @Test
    @SneakyThrows
    void execute() {
        ThreadUtils.execute(10, 100, integer -> log.info("{}", integer));
    }

    @Test
    @SneakyThrows
    void execute2() {
        List<String> list = IntStream.range(0, 100).boxed().map(String::valueOf).toList();
        ThreadUtils.execute(10, list, s -> log.info("{}", s));
    }

}
