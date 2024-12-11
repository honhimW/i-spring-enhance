package io.github.honhimw.example;

import java.util.concurrent.ThreadFactory;

/**
 * @author hon_him
 * @since 2023-12-25
 */

public class ThreadPool {

    public static ThreadFactory get() {
        return r -> new Thread(r, "platform thread");
    }

}
