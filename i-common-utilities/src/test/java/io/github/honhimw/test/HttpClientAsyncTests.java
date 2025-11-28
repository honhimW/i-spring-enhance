package io.github.honhimw.test;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.entity.AsyncEntityProducers;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

/**
 * @author hon_him
 * @since 2025-01-15
 */

public class HttpClientAsyncTests {

    @Test
    @SneakyThrows
    void h2() {
        @Cleanup
        CloseableHttpAsyncClient http = HttpAsyncClients.createHttp2Default();
        http.start();
        AsyncRequestBuilder post = AsyncRequestBuilder.create("POST");
        post.setUri("http://127.0.0.1:11451/post");
        AsyncEntityProducer asyncEntityProducer = AsyncEntityProducers.create("{}", ContentType.APPLICATION_JSON);
        post.setEntity(asyncEntityProducer);
        AsyncRequestProducer build = post.build();
        Future<SimpleHttpResponse> execute = http.execute(build, SimpleResponseConsumer.create(), new FutureCallback<>() {
            @Override
            public void completed(SimpleHttpResponse result) {

            }

            @Override
            public void failed(Exception ex) {

            }

            @Override
            public void cancelled() {

            }
        });
        SimpleHttpResponse simpleHttpResponse = execute.get();
        System.out.println(simpleHttpResponse.getBodyText());
    }

}
