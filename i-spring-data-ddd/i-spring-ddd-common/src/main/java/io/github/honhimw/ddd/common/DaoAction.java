package io.github.honhimw.ddd.common;

import lombok.Getter;

/**
 * @author hon_him
 * @since 2022-10-25
 */

@Getter
public enum DaoAction {

    INSERT(0),
    UPDATE(1),
    DELETE(2),
    SELECT(3),
    LOGIC_DELETE(4),

    NONE(-1), // for unconsidered situations
    ;

    private final Integer type;

    DaoAction(Integer type) {
        this.type = type;
    }
}