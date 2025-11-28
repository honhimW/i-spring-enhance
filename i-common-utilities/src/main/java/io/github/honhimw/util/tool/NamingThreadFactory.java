package io.github.honhimw.util.tool;

import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.ThreadFactory;

/**
 * In radix 36 by default
 *
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

    @SuppressWarnings("all")
    private volatile long count;
    private final String name;
    private final int radix;

    public NamingThreadFactory(ThreadFactory delegate, String prefix) {
        this(delegate, prefix, Character.MAX_RADIX);
    }

    public NamingThreadFactory(ThreadFactory delegate, String prefix, int radix) {
        super(delegate);
        this.name = prefix;
        this.count = 0L;
        this.radix = radix;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread thread = super.newThread(r);
        thread.setName(nextThreadName(getCount()));
        return thread;
    }

    protected long getCount() {
        return (long) COUNT.getAndAdd(this, 1);
    }

    protected String nextThreadName(long no) {
        return name + Long.toString(no, radix);
    }

}
