package io.github.honhimw.spring.statemachine;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.util.Optional;

/**
 * 简易FSM工厂
 *
 * @author hon_him
 * @since 2022-12-29
 */

@SuppressWarnings("unused")
public class IFSMFactory {

    private IFSMFactory() {
    }

    public static <S, E> StateMachine<S, E> getStateMachine(StateMachineConfigurerAdapter<S, E> adapter) {
        return getStateMachine(null, adapter);
    }

    public static <S, E> StateMachine<S, E> getStateMachine(S initialStates, StateMachineConfigurerAdapter<S, E> adapter) {
        Builder<S, E> builder = StateMachineBuilder.builder();
        StateMachineConfigurationConfigurer<S, E> configuration = builder.configureConfiguration();
        StateMachineStateConfigurer<S, E> states = builder.configureStates();
        StateMachineTransitionConfigurer<S, E> transitions = builder.configureTransitions();
        StateMachineModelConfigurer<S, E> model = builder.configureModel();
        try {
            adapter.configure(configuration);
            adapter.configure(states);
            adapter.configure(transitions);
            adapter.configure(model);
            StateMachine<S, E> stateMachine = builder.build();
            Optional.ofNullable(initialStates)
                .map(s -> new DefaultStateMachineContext<S, E>(initialStates, null, null, null))
                .ifPresent(context -> stateMachine.getStateMachineAccessor()
                    .doWithAllRegions(access -> stateMachine.stopReactively()
                        .then(access.resetStateMachineReactively(context))
                        .then(stateMachine.startReactively())
                        .subscribe()));
            return stateMachine;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
