package io.github.honhimw.spring;

import org.springframework.context.ApplicationEvent;

/**
 * @author hon_him
 * @since 2023-05-29
 */

public class ReBuildInEvent extends ApplicationEvent {

    public ReBuildInEvent(Object source) {
        super(source);
    }

}
