package io.github.honhimw.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author honhimW
 * @since 2025-07-04
 */

public class Okhttp5Tests {

    @Test
    @SneakyThrows
    void get() {
        OkHttpClient client = new OkHttpClient.Builder()
            .build();

        Request.Builder builder = new Request.Builder();
        builder
            .url("http://127.0.0.1:11451/hello?foo=bar")
            .get();
        @Cleanup
        Response execute = client.newCall(builder.build()).execute();
        ResponseBody body = execute.body();
        String string = body.byteString().string(StandardCharsets.UTF_8);
        System.out.println(string);
    }

    @Test
    @SneakyThrows
    void post() {
        OkHttpClient client = new OkHttpClient.Builder()
            .build();

        Request.Builder builder = new Request.Builder();
        RequestBody body = RequestBody.create("hello", okhttp3.MediaType.parse("text/plain"));
        builder.url("http://127.0.0.1:11451").post(body);
        Request request = builder.build();
        try (Response response = client.newCall(request).execute()) {
            String string = response.body().string();
            System.out.println(string);
        }
    }

}
