package io.github.honhimw.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author hon_him
 * @since 2024-11-25
 */

@Slf4j
public class ReactorTest {

    @Test
    @SneakyThrows
    void execute() {
        List<String> block = Flux.range(0, 100).map(Object::toString).collectSortedList().block();
        ReactorUtils.execute(10, block, log::info);
    }

}
