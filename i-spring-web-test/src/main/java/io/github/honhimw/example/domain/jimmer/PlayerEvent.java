package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.ddd.common.DaoAction;
import io.github.honhimw.ddd.common.DomainEvent;

/**
 * @author honhimW
 * @since 2025-06-04
 */

public class PlayerEvent extends DomainEvent<Player, String> {

    private final Player _do;

    public PlayerEvent(DaoAction action, Player player) {
        super(action);
        this._do = player;
    }

}
