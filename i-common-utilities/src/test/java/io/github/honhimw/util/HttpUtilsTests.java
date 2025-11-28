package io.github.honhimw.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-12-05
 */

@Slf4j
public class HttpUtilsTests {

    @Test
    @SneakyThrows
    void overrideContentType() {
        HttpUtils.HttpResult post = HttpUtils.getSharedInstance().post("http://127.0.0.1:11451/form-url-encoded", configurer -> configurer
                .charset(Charset.forName("GBK"))
                .body(body -> body.formUrlEncoded(formUrlEncoded -> formUrlEncoded
                        .text("foo", "bar")
                        .withCharset(true)
//                .contentType(ContentType.create("application/x-www-form-urlencoded", "utf-8"))
                ))
        );
        System.out.println(post.str());
    }

    @Test
    @SneakyThrows
    void noBodyFilter() {
        HttpUtils http = HttpUtils.newInstance(
            customizer -> customizer
                .filter(chainBuilder -> chainBuilder
                    .addFilterAt(HttpUtils.Stage.SET_ENTITY, HttpUtils.FilterChain::doFilter)
                    .addFilterBefore(HttpUtils.Stage.EXECUTE, (chain, ctx) -> {
                        assert ctx.getHttpRequest().getEntity() == null;
                        chain.doFilter(ctx);
                    })
                )
        );
        HttpUtils.HttpResult post = http.post("http://127.0.0.1:11451/post/json/no-body", configurer -> configurer.body(body -> body.raw(raw -> raw.json(Map.of("foo", "bar")))));
        System.out.println(post.str());
    }

    @Test
    @SneakyThrows
    void logElapsed() {
        HttpUtils http = HttpUtils.newInstance(customizer -> customizer
            .filter(chainBuilder -> chainBuilder
                .addFilterBefore(HttpUtils.Stage.EXECUTE, (chain, ctx) -> {
                    chain.doFilter(ctx);
                    log.info("elapsed: {}", ctx.getHttpResult().getElapsed());
                })
            )
        );
        HttpUtils.HttpResult post = http.post("http://127.0.0.1:11451/post/log-elapsed", configurer -> configurer
            .body(body -> body.raw(raw -> raw.json(Map.of("foo", "bar"))))
        );
        System.out.println(post.str());
    }

    @Test
    @SneakyThrows
    void rewriteBody() {
        HttpUtils http = HttpUtils.newInstance(customizer -> customizer
            .filter(chainBuilder -> chainBuilder
                .addFilterBefore(HttpUtils.Stage.SET_ENTITY, (chain, ctx) -> {
                    ctx.getConfigurer().body(body -> body.raw(raw -> raw.json(Map.of("hello", "world"))));
                    chain.doFilter(ctx);
                })
            )
        );
        HttpUtils.HttpResult post = http.post("http://127.0.0.1:11451/post/rewrite-body", configurer -> configurer
            .body(body -> body.formUrlEncoded(formUrlEncoded -> formUrlEncoded.text("foo", "bar"))));
        System.out.println(post.str());
    }

    @Test
    @SneakyThrows
    void brokenChain() {
        HttpUtils http = HttpUtils.newInstance(customizer -> customizer
            .filter(chainBuilder -> chainBuilder
                .addFilterAfter(HttpUtils.Stage.SET_ENTITY, (chain, ctx) -> ctx.getConfigurer().body(body -> body.raw(raw -> raw.json(Map.of("hello", "world")))))
            )
        );
        HttpUtils.HttpResult post = http.post("http://127.0.0.1:11451/post/rewrite-body", configurer -> configurer
            .body(body -> body.formUrlEncoded(formUrlEncoded -> formUrlEncoded.text("foo", "bar"))));
        assert post == null;
    }

    @Test
    @SneakyThrows
    void binary() {
        HttpUtils http = HttpUtils.getSharedInstance();
        HttpUtils.HttpResult post = http.post("http://127.0.0.1:11451/post/binary", configurer -> configurer
            .body(body -> body.binary(binary -> binary.bytes("hello".getBytes(StandardCharsets.UTF_8), ContentType.TEXT_PLAIN)))
        );
        System.out.println(post.str());
    }

    @Test
    @SneakyThrows
    void multipart() {
        HttpUtils http = HttpUtils.getSharedInstance();
        HttpUtils.HttpResult post = http.post("http://127.0.0.1:11451/post/binary", configurer -> configurer
            .body(body -> body.formData(formData -> formData
                .text("foo", "bar")
                .bytes("hello", "world".getBytes(StandardCharsets.UTF_8), ContentType.TEXT_PLAIN, "hello.txt")
            ))
        );
        post.getAllHeaders().put("Content-type", List.of("text/plain"));
        post.debug();
    }

    @Test
    @SneakyThrows
    void retryByResponseBody() {
        HttpUtils httpUtils = HttpUtils.newInstance(customizer -> customizer
            .filter(chainBuilder -> chainBuilder
                .addFilterBefore(HttpUtils.Stage.EXECUTE, (chain, ctx) -> {
                    for (int i = 0; i < 3; i++) {
                        chain.doFilter(ctx);
                        HttpUtils.HttpResult httpResult = ctx.getHttpResult();
                        System.out.println(httpResult.str());
                    }
                })
            )
        );

//        HttpUtils.HttpResult get = httpUtils.get("https://httpbin.dev/get");
//        System.out.println(get.getStatusLine());
//        System.out.println(get.str());
        FileInputStream fileInputStream = new FileInputStream("E:\\Gradle\\LICENSE");
        HttpUtils.HttpResult post = httpUtils.post("http://127.0.0.1:11451/", configurer -> configurer
            .body(body -> body.formData(formData -> formData.inputStream("foo", fileInputStream)))
        );
        System.out.println(post.getStatusLine());
        System.out.println(post.str());
    }

}
