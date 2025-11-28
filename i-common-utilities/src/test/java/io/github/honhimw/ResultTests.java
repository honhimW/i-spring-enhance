package io.github.honhimw;

import io.github.honhimw.util.tool.Result;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Objects;

/**
 * @author honhimW
 * @since 2025-08-08
 */

public class ResultTests {

    @Test
    @SneakyThrows
    void ok() {
        Result<Integer> ok = Result.ok(1);
        assert ok.isOk();
        assert ok.map(integer -> 2).unwrap() == 2;
        assert ok.match(Object::toString, throwable -> "2").equals("1");
        assert ok.option().isPresent();
        assert ok.unwrapOrDefault(3) == 1;
        assert ok.unwrapOrDefault(() -> 3) == 1;
        ok.match(integer -> {
            assert integer != null;
        }, throwable -> {
            throw new RuntimeException("will not throw");
        });
        assert ok.toString().equals("Ok(1)");
    }

    @Test
    @SneakyThrows
    void err() {
        Exception exception = new Exception("err");
        Result<Integer> err = Result.err(exception);
        assert err.isErr();
        err.map(integer -> {
            throw new RuntimeException("will not throw");
        });
        assert err.match(Objects::toString, throwable -> "2").equals("2");
        assert err.option().isEmpty();
        assert err.unwrapOrDefault(2) == 2;
        assert err.unwrapOrDefault(() -> 2) == 2;
        err.match(integer -> {
            throw new RuntimeException("will not throw");
        }, throwable -> {
            assert throwable != null;
        });
        assert err.toString().equals("Err(%s)".formatted(exception));

    }

}
