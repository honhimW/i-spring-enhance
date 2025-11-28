package io.github.honhimw.util;

import io.github.honhimw.util.tool.NamingThreadFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author hon_him
 * @since 2024-11-27
 */

@Slf4j
public class ThreadNameTest {

    private static final VarHandle COUNT;

    private volatile long count;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            COUNT = l.findVarHandle(ThreadNameTest.class, "count", long.class);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    @Test
    @SneakyThrows
    void plus() {
        this.count = Long.MAX_VALUE;
        assert ((long) COUNT.getAndAdd(this, 1)) == Long.MAX_VALUE;
        assert ((long) COUNT.getAndAdd(this, 1)) == Long.MIN_VALUE;

        for (int i = 0; i < 1000; i++) {
            System.out.println(BigInteger.valueOf(i).toString(36));
            System.out.println(Long.toString(i, 36));
        }
    }

    @Test
    @SneakyThrows
    void naming() {
        BasicThreadFactory.Builder builder = BasicThreadFactory.builder();
        builder.namingPattern("basic-%x");
        ThreadFactory factory = builder.build();
        factory = new NamingThreadFactory(factory, "test-");
        ExecutorService executorService = Executors.newFixedThreadPool(1000, factory);
        ThreadUtils.execute(executorService, 1000, 100, null, integer -> log.info("{}", integer));
    }

}
