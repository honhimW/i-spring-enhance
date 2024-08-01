package io.github.honhimw.spring.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.statemachine.action.StateDoActionPolicy;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

/**
 * @author hon_him
 * @since 2022-12-30
 */

public abstract class SimpleFSMAdapter<S extends Enum<S>, E extends Enum<E>> extends StateMachineConfigurerAdapter<S, E> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected ApplicationEventPublisher publisher;

    public void setPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    protected abstract S initialState();

    protected abstract S[] states();

    protected void publish(@Nonnull Object o) {
        Optional.ofNullable(publisher).ifPresent(__ -> __.publishEvent(o));
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<S, E> config) throws Exception {
        config
            .withConfiguration()
            .autoStartup(true)
            .stateDoActionPolicy(StateDoActionPolicy.IMMEDIATE_CANCEL)
        ;
    }

    @Override
    public void configure(StateMachineStateConfigurer<S, E> states) throws Exception {
        states
            .withStates()
            .initial(initialState())
            .states(Set.of(states()))
        ;
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<S, E> transitions) throws Exception {
        super.configure(transitions);
    }
}
