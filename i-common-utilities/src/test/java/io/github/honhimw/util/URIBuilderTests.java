package io.github.honhimw.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * @author honhimW
 * @since 2025-09-22
 */

public class URIBuilderTests {

    @Test
    @SneakyThrows
    void withoutSchema() {
        URIBuilder uriBuilder = new URIBuilder("localhost:8080");
        System.out.println(uriBuilder.getScheme());
    }

}
