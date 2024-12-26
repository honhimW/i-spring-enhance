package io.github.honhimw.spring.cache.redis;

import io.github.honhimw.spring.cache.TTLCache;
import io.github.honhimw.spring.cache.memory.InMemoryTTLCache;
import io.github.honhimw.util.ReactorUtils;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author hon_him
 * @since 2023-06-27
 */

public class MainRunner {

    @Test
    public void memory() throws InterruptedException {
        TTLCache<String, Object> inMemoryTTLCache = InMemoryTTLCache.newInstance(Duration.ofSeconds(10));
        ReactorUtils.execute(10, 100, integer -> {
            inMemoryTTLCache.put("a", 1, Duration.ofMillis(ThreadLocalRandom.current().nextLong(100, 555)));
            inMemoryTTLCache.put("b", 2);
            try {
                Thread.sleep(300L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread.sleep(11000L);
        inMemoryTTLCache.discard();
    }

    @Test
    public void queue() {
        Queue<Integer> integerQueue = new PriorityQueue<>();
        SortedSet<Integer> sortedSet = new ConcurrentSkipListSet<>();
        Integer i = 1;
        integerQueue.add(i);
        integerQueue.add(i);
        System.out.println(integerQueue.size());
    }

}
