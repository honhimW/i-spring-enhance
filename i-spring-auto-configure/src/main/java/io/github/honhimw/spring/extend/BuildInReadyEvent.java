package io.github.honhimw.spring.extend;

import org.springframework.context.ApplicationEvent;

/**
 * publish after {@link io.github.honhimw.spring.BuildIn} components have been executed successfully.
 * @author hon_him
 * @since 2023-09-08
 */

public class BuildInReadyEvent extends ApplicationEvent {
    public BuildInReadyEvent() {
        super(new Object());
    }
}
