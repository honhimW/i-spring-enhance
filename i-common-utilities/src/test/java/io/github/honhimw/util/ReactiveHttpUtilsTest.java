package io.github.honhimw.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author hon_him
 * @since 2025-01-17
 */

public class ReactiveHttpUtilsTest {

    @Test
    @SneakyThrows
    void execute() {
        ReactiveHttpUtils instance = ReactiveHttpUtils.getInstance();
        ReactiveHttpUtils.HttpResult result = instance.get("http://127.0.0.1:11451/post/json", configurer -> {
        });
        System.out.println(result.str());
    }

    @Test
    @SneakyThrows
    void sse() {
        ReactiveHttpUtils instance = ReactiveHttpUtils.getInstance();
        HttpClient.ResponseReceiver<?> responseReceiver = instance.rPost("http://127.0.0.1:11451/sse/custom", configurer -> configurer
            .body(payload -> payload.raw(raw -> raw.json("""
                [
                    {
                        "id": "a",
                        "name": "foo",
                        "comment": "bar",
                        "data": "{\\"foo\\":\\"bar\\"}",
                        "mediaType": "application/json"
                    }
                ]
                """)))
        );
        Flux<ByteBuf> response = responseReceiver.response((httpClientResponse, byteBufFlux) -> byteBufFlux);
        response.map(byteBuf -> {
                String string = byteBuf.toString(StandardCharsets.UTF_8);
                System.out.println(string);
                return string;
            })
            .collectSortedList().block();
    }

    @Test
    @SneakyThrows
    void block() {
        ReactiveHttpUtils instance = ReactiveHttpUtils.getInstance(builder -> builder
            .filters(chainBuilder -> chainBuilder.addFilterBefore(ReactiveHttpUtils.Stage.EXECUTE, (chain, ctx) -> {
                System.out.println(ctx.getAttributes());
                return chain.doFilter(ctx)
                    .doOnNext(httpResult -> {
                        System.out.println(httpResult.getElapsed());
                        System.out.println(httpResult.getStatusLine());
                    });
            })));
        ReactiveHttpUtils.HttpResult post = instance.post("http://127.0.0.1:11451/post/json", configurer -> configurer
            .body(payload -> payload.raw(raw -> raw
                .json(Map.of("foo", "bar")))
            ));
        System.out.println(post.str());
    }

    @Test
    @SneakyThrows
    void socks5() {
        ReactiveHttpUtils instance = ReactiveHttpUtils.getInstance();
        ReactiveHttpUtils.HttpResult get = instance.request(configurer -> configurer
            .method("GET").url("https://www.google.com")
            .config(builder -> builder.customize(httpClient -> httpClient
                    .proxy(typeSpec -> typeSpec
                        .type(ProxyProvider.Proxy.SOCKS5)
                        .host("192.168.0.126").port(10808)
                    )
                )
            )
        );
        System.out.println(get.str());

    }

    @Test
    @SneakyThrows
    void formData() {
        ReactiveHttpUtils instance = ReactiveHttpUtils.getInstance();
        ReactiveHttpUtils.HttpResult post = instance.post("http://127.0.0.1:11451/form", configurer -> configurer

            .body(payload -> payload.formData(formData -> formData
                .text("foo", "bar")
                .text("json", "{\"foo\": \"bar\"}", "application/json")
                .file("files", new File("E:\\temp\\video\\files.txt"), "application/json")
            ))
        );
        System.out.println(post.str());
    }

    @Test
    @SneakyThrows
    void reactive() {
        ReactiveHttpUtils instance = ReactiveHttpUtils.getInstance();
        Mono<Object> objectMono = instance.rGet("http://127.0.0.1:11451/test").responseSingle((response, byteBufMono) -> {
            HttpHeaders entries = response.responseHeaders();
            return Mono.empty();
        });
        objectMono.block();
    }

}
