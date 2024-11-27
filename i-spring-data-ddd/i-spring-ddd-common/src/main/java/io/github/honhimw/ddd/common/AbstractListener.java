package io.github.honhimw.ddd.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2022-11-29
 */

@SuppressWarnings("unused")
@Slf4j
public abstract class AbstractListener {

    protected void logEvent(DomainEvent<?, ?> event) {
        if (log.isDebugEnabled()) {
            log.debug("{}: action: {}, entity: {}", event.getClass().getSimpleName(), event.getAction(), event.getEntity());
        } else {
            log.info("{}: action: {}, entity: {}", event.getClass().getSimpleName(), event.getAction(), event.getId());
        }
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> beforeInsert(DomainEvent<T, ID> event) {
        return beforeAction(event, DaoAction.INSERT);
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> beforeUpdate(DomainEvent<T, ID> event) {
        return beforeAction(event, DaoAction.UPDATE);
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> beforeDelete(DomainEvent<T, ID> event) {
        return beforeAction(event, DaoAction.DELETE);
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> beforeLogicDelete(DomainEvent<T, ID> event) {
        return beforeAction(event, DaoAction.LOGIC_DELETE);
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> beforeSelect(DomainEvent<T, ID> event) {
        return beforeAction(event, DaoAction.SELECT);
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> beforeAction(DomainEvent<T, ID> event, DaoAction... action) {
        if (Objects.nonNull(event) && ArrayUtils.contains(action, event.getAction())) {
            return Optional.of(event).filter(DomainEvent::isPre);
        }
        return Optional.empty();
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> afterInsert(DomainEvent<T, ID> event) {
        return afterAction(event, DaoAction.INSERT);
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> afterUpdate(DomainEvent<T, ID> event) {
        return afterAction(event, DaoAction.UPDATE);
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> afterDelete(DomainEvent<T, ID> event) {
        return afterAction(event, DaoAction.DELETE);
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> afterLogicDelete(DomainEvent<T, ID> event) {
        return afterAction(event, DaoAction.LOGIC_DELETE);
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> afterSelect(DomainEvent<T, ID> event) {
        return afterAction(event, DaoAction.SELECT);
    }

    protected <T, ID> Optional<DomainEvent<T, ID>> afterAction(DomainEvent<T, ID> event, DaoAction... action) {
        if (Objects.nonNull(event) && ArrayUtils.contains(action, event.getAction())) {
            return Optional.of(event).filter(DomainEvent::isPost);
        }
        return Optional.empty();
    }

    @Deprecated
    protected <T, ID> Optional<DomainEvent<T, ID>> onInsert(DomainEvent<T, ID> event) {
        return onAction(event, DaoAction.INSERT);
    }

    @Deprecated
    protected <T, ID> Optional<DomainEvent<T, ID>> onUpdate(DomainEvent<T, ID> event) {
        return onAction(event, DaoAction.UPDATE);
    }

    @Deprecated
    protected <T, ID> Optional<DomainEvent<T, ID>> onDelete(DomainEvent<T, ID> event) {
        return onAction(event, DaoAction.DELETE);
    }

    @Deprecated
    protected <T, ID> Optional<DomainEvent<T, ID>> onLogicDelete(DomainEvent<T, ID> event) {
        return onAction(event, DaoAction.LOGIC_DELETE);
    }

    @Deprecated
    protected <T, ID> Optional<DomainEvent<T, ID>> onSelect(DomainEvent<T, ID> event) {
        return onAction(event, DaoAction.SELECT);
    }

    @Deprecated
    protected <T, ID> Optional<DomainEvent<T, ID>> onAction(DomainEvent<T, ID> event, DaoAction... action) {
        if (Objects.nonNull(event) && ArrayUtils.contains(action, event.getAction())) {
            return Optional.of(event);
        }
        return Optional.empty();
    }

}
