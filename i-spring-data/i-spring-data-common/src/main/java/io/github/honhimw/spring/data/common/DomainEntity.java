package io.github.honhimw.spring.data.common;

import java.util.function.Function;

/**
 * @author hon_him
 * @since 2022-10-17
 */
public interface DomainEntity<A extends DomainEntity<A, ID>, ID> {

    ID getId();

    default Function<DaoAction, ? extends DomainEvent<A, ID>> eventBuilder() {
        return action -> null;
    }

}
