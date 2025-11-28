package io.github.honhimw.util.tool;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * @author hon_him
 * @since 2024-11-27
 */

public class DelegateThreadFactory implements ThreadFactory {

    private final ThreadFactory delegate;

    public DelegateThreadFactory(ThreadFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        return delegate.newThread(r);
    }

    public ThreadFactory getDelegate() {
        return delegate;
    }
}
