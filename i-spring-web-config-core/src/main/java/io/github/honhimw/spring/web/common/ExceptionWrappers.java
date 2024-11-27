package io.github.honhimw.spring.web.common;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class ExceptionWrappers {

    private volatile int MAX_UNWRAP_DEPTH = 5;

    private final List<ExceptionWrapper> exceptionWrappers;

    public ExceptionWrappers(List<ExceptionWrapper> exceptionWrappers) {
        if (CollectionUtils.isEmpty(exceptionWrappers)) {
            this.exceptionWrappers = new ArrayList<>();
        } else {
            this.exceptionWrappers = new ArrayList<>(exceptionWrappers);
        }
    }

    public void setMaxUnwrapDepth(int depth) {
        Assert.state(depth > 0, "depth must greater than 0.");
        this.MAX_UNWRAP_DEPTH = depth;
    }

    public ExceptionWrapper getWrapper(Throwable e) {
        ExceptionWrapper ew;
        Throwable t = e;
        int deepth = 0;
        do {
            Throwable finalT = t;
            ew = exceptionWrappers.stream()
                .filter(wrapper -> wrapper.support(finalT))
                .findFirst()
                .orElse(ExceptionWrapper.DEFAULT);
        } while (ew.unwrapCause()
            && deepth++ < MAX_UNWRAP_DEPTH
            && Objects.nonNull((t = t.getCause()))
        );
        return ew;
    }

    public Object handle(Throwable e) {
        ExceptionWrapper ew;
        Throwable t = e;
        int deepth = 0;
        AtomicReference<Throwable> ref = new AtomicReference<>();
        do {
            ref.set(t);
            ew = exceptionWrappers.stream()
                .filter(wrapper -> wrapper.support(ref.get()))
                .findFirst()
                .orElse(ExceptionWrapper.DEFAULT);
        } while (ew.unwrapCause()
            && deepth++ < MAX_UNWRAP_DEPTH
            && Objects.nonNull((t = t.getCause()))
        );
        return ew.wrap(t);
    }

    public <T> T handle(Throwable e, BiFunction<ExceptionWrapper, Throwable, T> hook) {
        ExceptionWrapper ew;
        Throwable t = e;
        int deepth = 0;
        AtomicReference<Throwable> ref = new AtomicReference<>();
        do {
            ref.set(t);
            ew = exceptionWrappers.stream()
                .filter(wrapper -> wrapper.support(ref.get()))
                .findFirst()
                .orElse(ExceptionWrapper.DEFAULT);
        } while (ew.unwrapCause()
            && deepth++ < MAX_UNWRAP_DEPTH
            && Objects.nonNull((t = t.getCause()))
        );
        return hook.apply(ew, ref.get());
    }

}
