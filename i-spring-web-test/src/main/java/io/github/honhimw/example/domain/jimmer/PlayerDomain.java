package io.github.honhimw.example.domain.jimmer;


import io.github.honhimw.ddd.common.DaoAction;
import io.github.honhimw.ddd.common.DomainEvent;
import io.github.honhimw.ddd.jimmer.event.DomainEntity;

import java.util.function.Function;

/**
 * @author honhimW
 * @since 2025-06-05
 */

public interface PlayerDomain extends DomainEntity {

    @Override
    default Function<DaoAction, ? extends DomainEvent<?, ?>> eventBuilder() {
        if (this instanceof Player player) {
            return daoAction -> new PlayerEvent(daoAction, player);
        }
        return DomainEntity.super.eventBuilder();
    }
}
