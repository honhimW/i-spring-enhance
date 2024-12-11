package io.github.honhimw.spring.statemachine;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2023-01-10
 */
@SuppressWarnings({"unused"})
public interface FSMService<S, E, D> {

    /**
     * Get new state machine instance from entity
     *
     * @param d data object
     * @return state machine
     */
    StateMachine<S, E> getFSM(D d);

    /**
     * 1. Data object to state machine instance;
     * 2. Send event to state machine;
     * 3. Hook callback and return result;
     *
     * @param d           data object
     * @param event       event
     * @param successHook success hook
     * @param failHook    fail hook
     * @return result
     */
    default <EX extends Exception> Mono<Boolean> rSend(D d, E event, Consumer<D> successHook,
                                                       Function<Exception, EX> failHook) {
        StateMachine<S, E> fsm = getFSM(d);
        Flux<StateMachineEventResult<S, E>> stateMachineEventResultFlux = fsm.sendEvent(
            FSMUtils.eventMsg(event));

        return stateMachineEventResultFlux.next()
            .handle((result, sink) -> {
                ResultType resultType = result.getResultType();
                switch (resultType) {
                    case DENIED -> Optional.ofNullable(failHook)
                        .map(_function -> _function.apply(new IllegalStateException(
                            String.format("current state: [%s] reject handling event: [%s]", fsm.getState().getId(), event))))
                        .ifPresentOrElse(sink::error, () -> sink.next(false));
                    case DEFERRED -> Optional.ofNullable(failHook)
                        .map(_function -> _function.apply(new IllegalStateException(
                            String.format("current state: [%s] deferred handling event: [%s]", fsm.getState().getId(), event))))
                        .ifPresentOrElse(sink::error, () -> sink.next(false));
                    case ACCEPTED -> {
                        Exception exception = FSMUtils.guardResult(result);
                        if (Objects.nonNull(exception)) {
                            Optional.ofNullable(failHook)
                                .map(_function -> _function.apply(exception))
                                .ifPresentOrElse(sink::error, () -> sink.next(false));
                        } else {
                            successHook.accept(d);
                            sink.next(true);
                        }
                    }
                    default -> Optional.ofNullable(failHook)
                        .map(_function -> _function.apply(new IllegalStateException("not supposed to happen")))
                        .ifPresent(sink::error);
                }
            });
    }

    default <EX extends Exception> Mono<Boolean> rSend(D d, E e,
                                                       Function<Exception, EX> failHook) {
        return rSend(d, e, d1 -> {
        }, failHook);
    }

    default <EX extends Exception> Mono<Boolean> rSend(D d, E e,
                                                       Consumer<D> successHook) {
        return rSend(d, e, successHook, IllegalStateException::new);
    }

    default Mono<Boolean> rSend(D d, E e) {
        return rSend(d, e, d1 -> {
        }, IllegalArgumentException::new);
    }

    /**
     * 1. Data object to state machine instance;
     * 2. Send event to state machine;
     * 3. Hook callback and return result;
     *
     * @param d           data object
     * @param event       event
     * @param successHook success hook
     * @param failHook    fail hook
     * @return result
     */
    default <EX extends Exception> boolean send(D d, E event, Consumer<D> successHook,
                                                Function<Exception, EX> failHook) throws EX {
        StateMachine<S, E> fsm = getFSM(d);
        Flux<StateMachineEventResult<S, E>> stateMachineEventResultFlux = fsm.sendEvent(
            FSMUtils.eventMsg(event));

        StateMachineEventResult<S, E> stateResult;
        try {
            stateResult = stateMachineEventResultFlux.next().toFuture().get();
        } catch (Exception e) {
            throw failHook.apply(e);
        }

        Objects.requireNonNull(stateResult, "FSM result would not be null");
        ResultType resultType = stateResult.getResultType();
        switch (resultType) {
            case DENIED -> {
                EX apply = failHook.apply(
                    new IllegalStateException(
                        String.format("current state: [%s] reject handling event: [%s]", fsm.getState().getId(), event)));
                if (Objects.nonNull(apply)) {
                    throw apply;
                }
                return false;
            }
            case DEFERRED -> {
                EX apply = failHook.apply(
                    new IllegalStateException(
                        String.format("current state: [%s] deferred handling event: [%s]", fsm.getState().getId(), event)));
                if (Objects.nonNull(apply)) {
                    throw apply;
                }
                return false;
            }
            case ACCEPTED -> {
                Exception exception = FSMUtils.guardResult(stateResult);
                if (Objects.nonNull(exception)) {
                    EX apply = failHook.apply(exception);
                    if (Objects.nonNull(apply)) {
                        throw apply;
                    }
                    return false;
                } else {
                    successHook.accept(d);
                    return true;
                }
            }
            default -> throw new IllegalStateException("not supposed to happen");
        }
    }

    default <EX extends Exception> boolean send(D d, E e,
                                                Function<Exception, EX> failHook) throws EX {
        return send(d, e, d1 -> {
        }, failHook);
    }

    default <EX extends Exception> boolean send(D d, E e,
                                                Consumer<D> successHook) throws EX {
        return send(d, e, successHook, IllegalStateException::new);
    }

    default boolean send(D d, E e) {
        return send(d, e, d1 -> {
        }, IllegalArgumentException::new);
    }
}
