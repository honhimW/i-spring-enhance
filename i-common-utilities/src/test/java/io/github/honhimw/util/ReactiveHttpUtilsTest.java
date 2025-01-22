package io.github.honhimw.util;

import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
        Flux<ByteBuf> response = responseReceiver.response((httpClientResponse, byteBufFlux) -> {
            return byteBufFlux;
        });
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
                    Duration elapsed = ctx.get("elapsed");
                    System.out.println(elapsed);
                    System.out.println(httpResult.getStatusLine());
                });
        })));
        ReactiveHttpUtils.HttpResult post = instance.post("http://127.0.0.1:11451/post/json", configurer -> configurer
            .body(payload -> payload.raw(raw -> raw
                .json(Map.of("foo", "bar")))
            ));
        System.out.println(post.str());
    }

}
