package io.github.honhimw.spring.statemachine;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2022-12-30
 */
@SuppressWarnings("unused")
public class FSMUtils {

    public static final String RESULT_HOLDER = "RESULT_HOLDER";
    public static final String GUARD_DENY_EXCEPTION = "GUARD_DENY_EXCEPTION";

    public static <E> Mono<Message<E>> eventMsg(E event) {
        return Mono.just(MessageBuilder
            .withPayload(event)
            .setHeader(RESULT_HOLDER, new HashMap<String, Object>())
            .build());
    }

    public static <S, E> boolean result(Flux<StateMachineEventResult<S, E>> resultFlux) {
        try {
            return Boolean.TRUE.equals(resultFlux
                .map(StateMachineEventResult::getResultType)
                .map(resultType1 -> !Objects.equals(resultType1, ResultType.DENIED))
                .next()
                .toFuture()
                .get());
        } catch (Exception e) {
            return false;
        }
    }

    public static <S, E> boolean result(StateMachineEventResult<S, E> stateMachineEventResult) {
        return !Objects.equals(stateMachineEventResult.getResultType(), ResultType.DENIED);
    }

    public static <S, E> Exception guardDenyException(StateMachineEventResult<S, E> stateMachineEventResult) {
        return stateMachineEventResult.getMessage().getHeaders()
            .get(GUARD_DENY_EXCEPTION, Exception.class);
    }

    @SuppressWarnings("unchecked")
    public static <S, E> void guardDeny(StateContext<S, E> context, Exception e) {
        Optional.of(context)
            .map(StateContext::getMessage)
            .map(Message::getHeaders)
            .map(messageHeaders -> messageHeaders.get(FSMUtils.RESULT_HOLDER))
            .map(o -> (Map<String, Object>) o)
            .ifPresent(resultHolder -> resultHolder.putIfAbsent(FSMUtils.GUARD_DENY_EXCEPTION, e));
        ;
    }

    public static <S, E> Exception guardResult(StateMachineEventResult<S, E> stateMachineEventResult) {
        return (Exception) Optional.of(stateMachineEventResult)
            .map(StateMachineEventResult::getMessage)
            .map(Message::getHeaders)
            .map(messageHeaders -> messageHeaders.get(FSMUtils.RESULT_HOLDER))
            .map(o -> (Map<?, ?>) o)
            .map(resultHolder -> resultHolder.get(FSMUtils.GUARD_DENY_EXCEPTION))
            .orElse(null);
    }

}
