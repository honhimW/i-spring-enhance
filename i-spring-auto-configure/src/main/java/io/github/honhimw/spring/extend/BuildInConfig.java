package io.github.honhimw.spring.extend;

import io.github.honhimw.spring.BuildIn;
import io.github.honhimw.spring.DoBuildInEvent;
import jakarta.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.SmartApplicationListener;

import java.util.List;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2023-04-21
 */

class BuildInConfig implements SmartApplicationListener, ApplicationContextAware {

    private final List<BuildIn> buildInList;

    private ApplicationEventPublisher publisher;

    public BuildInConfig(List<BuildIn> buildInList) {
        this.buildInList = buildInList;
    }

    @Override
    public boolean supportsEventType(@Nonnull Class<? extends ApplicationEvent> eventType) {
        return DoBuildInEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void onApplicationEvent(@Nonnull ApplicationEvent event) {
        synchronized (this) {
            setup();
        }
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        publisher = applicationContext;
        onApplicationEvent(new DoBuildInEvent("ApplicationContextAware"));
    }

    protected void setup() {
        if (CollectionUtils.isNotEmpty(buildInList)) {
            List<BuildIn> sorted = buildInList.stream().sorted().toList();
            sorted.forEach(BuildIn::setup);
            Optional.ofNullable(publisher).ifPresent(applicationEventPublisher -> applicationEventPublisher.publishEvent(new BuildInReadyEvent()));
        }
    }

}
