package io.github.honhimw.spring;

import io.github.honhimw.spring.statemachine.FSMService;
import io.github.honhimw.spring.statemachine.FSMUtils;
import io.github.honhimw.spring.statemachine.IFSMFactory;
import io.github.honhimw.spring.statemachine.SimpleFSMAdapter;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

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
        S1,S2
    }

    enum E {
        E1,E2
    }

    public static void main(String[] args) throws Exception {
        // 每一次send都重新创建状态机
        AtomicInteger i = new AtomicInteger(0);
        FSMService<S, E, AtomicInteger> fsmService = _i -> IFSMFactory.getStateMachine(S.values()[_i.get()], new Ts(_i));
        fsmService.send(i, E.E1);
        fsmService.send(i, E.E2);

        // 同一个状态机多次接受事件
        StateMachine<S, E> fsm = fsmService.getFSM(i);
        fsm.sendEvent(FSMUtils.eventMsg(E.E1)).next().toFuture().get();
        fsm.sendEvent(FSMUtils.eventMsg(E.E2)).next().toFuture().get();
    }

}
