package io.github.honhimw.spring.web.common;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class ExceptionWrappers {

    private volatile int MAX_UNWRAP_DEPTH = 5;

    private final ObjectProvider<ExceptionWrapper> exceptionWrappers;

    public ExceptionWrappers(ObjectProvider<ExceptionWrapper> exceptionWrappers) {
        this.exceptionWrappers = exceptionWrappers;
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
            ew = exceptionWrappers.orderedStream()
                .filter(wrapper -> wrapper.support(finalT))
                .findFirst()
                .orElse(ExceptionWrapper.DEFAULT);
        } while (ew.unwrapCause()
                 && deepth++ < MAX_UNWRAP_DEPTH
                 && Objects.nonNull((t = t.getCause()))
        );
        return ew;
    }

    public <T> T handle(Throwable e, BiFunction<ExceptionWrapper, Throwable, T> hook) {
        ExceptionWrapper ew;
        Throwable t = e;
        int depth = 0;
        AtomicReference<Throwable> ref = new AtomicReference<>();
        do {
            ref.set(t);
            ew = exceptionWrappers.orderedStream()
                .filter(wrapper -> wrapper.support(ref.get()))
                .min(Comparator.comparingInt(ExceptionWrapper::getOrder))
                .orElse(ExceptionWrapper.DEFAULT);
        } while (ew.unwrapCause()
            && depth++ < MAX_UNWRAP_DEPTH
            && Objects.nonNull((t = t.getCause()))
        );
        T apply = hook.apply(ew, ref.get());
        if (ew instanceof ExceptionWrapper.MessageExceptionWrapper) {
            return hook.apply(ew, ref.get());
        }

        return apply;
    }

}
