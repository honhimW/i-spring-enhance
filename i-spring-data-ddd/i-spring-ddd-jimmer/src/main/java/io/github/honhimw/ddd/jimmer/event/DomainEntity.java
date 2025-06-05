package io.github.honhimw.ddd.jimmer.event;

import io.github.honhimw.ddd.common.DaoAction;
import io.github.honhimw.ddd.common.DomainEvent;

import java.util.function.Function;

/**
 * In Jimmer, you can neither use generics at the class level nor define default methods (except those annotated with {@link org.babyfish.jimmer.Formula}).
 * You can achieve this functionality by adding an intermediate layer interface.
 * <p>
 * Example:
 * <pre>{@code
 * public interface FooDomain extends DomainEntity {
 *     @Override
 *     default Function<DaoAction, ? extends DomainEvent<?, ?>> eventBuilder() {
 *         if (this instanceof Foo foo) {
 *             return daoAction -> new FooEvent(daoAction, foo);
 *         }
 *         return DomainEntity.super.eventBuilder();
 *     }
 * }
 *
 * @Entity
 * public interface Foo extends FooDomain {}
 * }</pre>
 *
 * @author honhimW
 * @since 2025-06-05
 */

public interface DomainEntity {

    default Function<DaoAction, ? extends DomainEvent<?, ?>> eventBuilder() {
        return action -> new DomainEvent<>(action).entity(this);
    }

}
