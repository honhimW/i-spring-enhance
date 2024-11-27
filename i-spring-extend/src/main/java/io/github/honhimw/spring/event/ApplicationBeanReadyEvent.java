package io.github.honhimw.spring.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

/**
 * Triggered after all non-lazy beans is ready. Before the web server starts(port binding).
 * May do something before the web server starts.
 *
 * @author hon_him
 * @since 2024-11-21
 */

public class ApplicationBeanReadyEvent extends ApplicationEvent {

    public ApplicationBeanReadyEvent(ApplicationContext context) {
        super(context);
    }

    public ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}
