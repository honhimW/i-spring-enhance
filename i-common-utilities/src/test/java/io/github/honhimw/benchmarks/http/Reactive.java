package io.github.honhimw.benchmarks.http;

import io.github.honhimw.util.ReactiveHttpUtils;

/**
 * @author honhimW
 * @since 2025-07-04
 */

public class Reactive extends AbstractHttpBenchMarks {

    ReactiveHttpUtils client;

    @Override
    public void setup() {
        super.setup();
        client = ReactiveHttpUtils.getInstance();
    }

    @Override
    public void get() throws Exception {
        String str = client.get("http://127.0.0.1:11451?foo=bar").str();
    }

    @Override
    public void post() throws Exception {
        String str = client.post("http://127.0.0.1:11451", configurer -> configurer.body(body -> body.raw(raw -> raw.text(data)))).str();
    }
}
