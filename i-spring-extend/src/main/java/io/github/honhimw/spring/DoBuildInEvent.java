package io.github.honhimw.spring;

import org.springframework.context.ApplicationEvent;

/**
 * @author hon_him
 * @since 2023-05-29
 */

public class DoBuildInEvent extends ApplicationEvent {

    public DoBuildInEvent(Object source) {
        super(source);
    }

}
