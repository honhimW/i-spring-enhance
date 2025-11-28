package io.github.honhimw.spring.extend;

import io.github.honhimw.spring.BuildIn;
import io.github.honhimw.spring.ReBuildInEvent;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.SmartApplicationListener;

import java.util.Optional;

/**
 * @author hon_him
 * @since 2023-04-21
 */

class BuildInConfig implements SmartApplicationListener, ApplicationEventPublisherAware, SmartInitializingSingleton {

    private final ObjectProvider<BuildIn> buildInProvider;

    private ApplicationEventPublisher publisher;

    public BuildInConfig(ObjectProvider<BuildIn> buildInProvider) {
        this.buildInProvider = buildInProvider;
    }

    @Override
    public boolean supportsEventType(@NonNull Class<? extends ApplicationEvent> eventType) {
        return ReBuildInEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        synchronized (this) {
            setup();
        }
    }

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }

    @Override
    public void afterSingletonsInstantiated() {
        setup();
    }

    protected void setup() {
        buildInProvider.orderedStream().forEach(BuildIn::setup);
        Optional.ofNullable(publisher).ifPresent(applicationEventPublisher -> applicationEventPublisher.publishEvent(new BuildInReadyEvent()));
    }

}
