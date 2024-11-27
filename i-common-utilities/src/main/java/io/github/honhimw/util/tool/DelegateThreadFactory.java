package io.github.honhimw.util.tool;

import jakarta.annotation.Nonnull;

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
    public Thread newThread(@Nonnull Runnable r) {
        return delegate.newThread(r);
    }

    public ThreadFactory getDelegate() {
        return delegate;
    }
}
