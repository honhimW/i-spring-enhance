package io.github.honhimw.ddd.common;

/**
 * Mark the logic delete event, DomainEntity implements LogicDelete
 * <pre>{@code
 * class DomainEntity implements LogicDelete {}
 * }</pre>
 *
 * @author hon_him
 * @since 2022-10-27
 */

public interface LogicDelete {

    boolean isDeleted();

}
