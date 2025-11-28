package io.github.honhimw.test;

import io.github.honhimw.util.ReactiveHttpUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

/**
 * @author hon_him
 * @since 2024-12-05
 */

public class ReactiveHttpUtilsTests {

    @Test
    @SneakyThrows
    void overrideContentType() {
        ReactiveHttpUtils.HttpResult post = ReactiveHttpUtils.getInstance().post("http://127.0.0.1:11451/form-url-encoded", configurer -> configurer
            .charset(Charset.forName("GBK"))
            .body(body -> body.formUrlEncoded(formUrlEncoded -> formUrlEncoded
                .text("foo", "bar")
            ))
        );
        System.out.println(post.str());
    }


}
