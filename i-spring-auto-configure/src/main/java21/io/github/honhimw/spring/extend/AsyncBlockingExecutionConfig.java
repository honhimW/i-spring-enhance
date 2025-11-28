package io.github.honhimw.spring.extend;

import io.github.honhimw.util.tool.NamingThreadFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.concurrent.ThreadFactory;

/**
 * @author hon_him
 * @since 2024-11-27
 */

public class AsyncBlockingExecutionConfig implements WebFluxConfigurer {

    @Override
    public void configureBlockingExecution(@NonNull BlockingExecutionConfigurer configurer) {
        configurer.setExecutor(new VirtualThreadTaskExecutor("loom-rx-"));
    }

    public static class VirtualThreadTaskExecutor implements AsyncTaskExecutor {
        private final ThreadFactory virtualThreadFactory;

        public VirtualThreadTaskExecutor() {
            this.virtualThreadFactory = Thread.ofVirtual().factory();
        }

        public VirtualThreadTaskExecutor(String threadNamePrefix) {
            this.virtualThreadFactory = new NamingThreadFactory(Thread.ofVirtual().factory(), threadNamePrefix);
        }

        public final ThreadFactory getVirtualThreadFactory() {
            return this.virtualThreadFactory;
        }

        public void execute(@NonNull Runnable task) {
            this.virtualThreadFactory.newThread(task).start();
        }
    }

}
