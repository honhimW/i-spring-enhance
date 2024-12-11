package io.github.honhimw.spring;

import io.github.honhimw.spring.statemachine.FSMService;
import io.github.honhimw.spring.statemachine.FSMUtils;
import io.github.honhimw.spring.statemachine.IFSMFactory;
import io.github.honhimw.spring.statemachine.SimpleFSMAdapter;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hon_him
 * @since 2023-07-06
 */

public class Ts extends SimpleFSMAdapter<Ts.S, Ts.E> {

    private final AtomicInteger _i;

    public Ts(AtomicInteger i) {
        _i = i;
    }

    @Override
    protected S initialState() {
        return S.S1;
    }

    @Override
    protected S[] states() {
        return S.values();
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<S, E> config) throws Exception {
        super.configure(config);
        config.withConfiguration()
            .listener(new StateMachineListenerAdapter<>() {
            @Override
            public void eventNotAccepted(Message<E> event) {
                super.eventNotAccepted(event);
                log.info("not accepted: {}", event.getPayload());
            }
        });
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<S, E> transitions) throws Exception {
        transitions.withExternal()
            .source(S.S1).target(S.S2).event(E.E1)
            .action(context -> {
                _i.getAndIncrement();
                log.info("S1 -> S2 on E1");
            })
            .and()
            .withExternal()
            .source(S.S2).target(S.S1).event(E.E2)
            .action(context -> {
                _i.getAndDecrement();
                log.info("S2 -> S1 on E2");
            })
        ;
    }

    enum S {
        S1, S2
    }

    enum E {
        E1, E2, E3
    }

    public static void main(String[] args) throws Exception {
        AtomicInteger i = new AtomicInteger(0);
        FSMService<S, E, AtomicInteger> fsmService = _i -> IFSMFactory.getStateMachine(S.values()[_i.get()], new Ts(_i));
        fsmService.send(i, E.E1);
        fsmService.send(i, E.E2);

        StateMachine<S, E> fsm = fsmService.getFSM(i);
        fsm.sendEvent(FSMUtils.eventMsg(E.E1)).next().block();
        fsm.sendEvent(FSMUtils.eventMsg(E.E2)).next().block();
        fsm.sendEvent(FSMUtils.eventMsg(E.E3)).next().block();
    }

}
