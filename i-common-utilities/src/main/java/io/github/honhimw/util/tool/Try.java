package io.github.honhimw.util.tool;

import org.jspecify.annotations.NullUnmarked;

import java.util.concurrent.Callable;

/**
 * @author honhimW
 * @since 2025-08-18
 */

@NullUnmarked
public class Try {

    private Try() {
    }

    public static Result<Void> run(Lambdas.Run<?> run) {
        try {
            run.run();
            return Result.ok();
        } catch (Throwable e) {
            return Result.err(e);
        }
    }

    public static <T> Result<T> call(Callable<T> call) {
        try {
            T t = call.call();
            return Result.ok(t);
        } catch (Throwable e) {
            return Result.err(e);
        }
    }

}
