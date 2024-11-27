package io.github.honhimw.util.tool;

import jakarta.annotation.Nonnull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.ThreadFactory;

/**
 * @author hon_him
 * @since 2024-11-27
 */
public class NamingThreadFactory extends DelegateThreadFactory {
    private static final VarHandle COUNT;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            COUNT = l.findVarHandle(NamingThreadFactory.class, "count", long.class);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    private volatile long count;
    private final String name;

    public NamingThreadFactory(ThreadFactory delegate, String prefix) {
        super(delegate);
        this.name = prefix;
        this.count = 0L;
    }

    @Override
    public Thread newThread(@Nonnull Runnable r) {
        Thread thread = super.newThread(r);
        thread.setName(nextThreadName(getCount()));
        return thread;
    }

    protected long getCount() {
        return (long) COUNT.getAndAdd(this, 1);
    }

    protected String nextThreadName(long no) {
        return name + Long.toString(no, 36);
    }
}
