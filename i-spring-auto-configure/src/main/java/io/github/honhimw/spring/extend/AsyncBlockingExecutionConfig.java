package io.github.honhimw.spring.extend;

import org.jspecify.annotations.NonNull;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * @author hon_him
 * @since 2024-11-27
 */

public class AsyncBlockingExecutionConfig implements WebFluxConfigurer {

    @Override
    public void configureBlockingExecution(@NonNull BlockingExecutionConfigurer configurer) {
    }

}
