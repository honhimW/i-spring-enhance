package io.github.honhimw.benchmarks.http;

import okhttp3.*;

/**
 * @author honhimW
 * @since 2025-07-04
 */

public class Okhttp5PerRequest extends AbstractHttpBenchMarks {

    OkHttpClient client;

    MediaType TEXT_PLAIN = MediaType.parse("text/plain");

    @Override
    public void setup() {
        super.setup();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(chain -> chain.proceed(chain.request()));
        client = new OkHttpClient();
    }

    @Override
    public void get() throws Exception {
        Request.Builder builder = new Request.Builder();
        builder.url("http://127.0.0.1:11451?foo=bar").get();
        Request request = builder.build();
        OkHttpClient client = this.client.newBuilder().build();
        try (Response response = client.newCall(request).execute()) {
            String string = response.body().string();
        }
    }

    @Override
    public void post() throws Exception {
        Request.Builder builder = new Request.Builder();
        RequestBody body = RequestBody.create(data, TEXT_PLAIN);
        builder.url("http://127.0.0.1:11451").post(body);
        Request request = builder.build();
        OkHttpClient client = this.client.newBuilder().build();
        try (Response response = client.newCall(request).execute()) {
            String string = response.body().string();
        }
    }
}
