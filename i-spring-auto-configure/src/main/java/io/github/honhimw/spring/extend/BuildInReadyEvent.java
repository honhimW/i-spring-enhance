package io.github.honhimw.spring.extend;

import io.github.honhimw.spring.BuildIn;
import org.springframework.context.ApplicationEvent;

/**
 * publish after {@link BuildIn} components have been executed successfully.
 * @author hon_him
 * @since 2023-09-08
 */

public class BuildInReadyEvent extends ApplicationEvent {
    public BuildInReadyEvent() {
        super(new Object());
    }
}
