package io.github.honhimw.ddd.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * You can use this as a base class for DomainListener, or call it directly with {@link #INSTANCE}.
 *
 * @author hon_him
 * @since 2024-12-10
 */

@Slf4j
@SuppressWarnings("unused")
public class DomainListenerSupports {

    public static final DomainListenerSupports INSTANCE = new DomainListenerSupports();

    public void logEvent(DomainEvent<?, ?> event) {
        if (log.isDebugEnabled()) {
            log.debug("{}: action: {}, entity: {}", event.getClass().getSimpleName(), event.getAction(), event.getEntity());
        } else {
            log.info("{}: action: {}, entity: {}", event.getClass().getSimpleName(), event.getAction(), event.getId());
        }
    }

    public <T, ID> Optional<DomainEvent<T, ID>> beforeInsert(DomainEvent<T, ID> event) {
        return beforeAction(event, DaoAction.INSERT);
    }

    public <T, ID> Optional<DomainEvent<T, ID>> beforeUpdate(DomainEvent<T, ID> event) {
        return beforeAction(event, DaoAction.UPDATE);
    }

    public <T, ID> Optional<DomainEvent<T, ID>> beforeDelete(DomainEvent<T, ID> event) {
        return beforeAction(event, DaoAction.DELETE);
    }

    public <T, ID> Optional<DomainEvent<T, ID>> beforeLogicDelete(DomainEvent<T, ID> event) {
        return beforeAction(event, DaoAction.LOGIC_DELETE);
    }

    public <T, ID> Optional<DomainEvent<T, ID>> beforeSelect(DomainEvent<T, ID> event) {
        return beforeAction(event, DaoAction.SELECT);
    }

    public <T, ID> Optional<DomainEvent<T, ID>> beforeAction(DomainEvent<T, ID> event, DaoAction... action) {
        if (Objects.nonNull(event) && ArrayUtils.contains(action, event.getAction())) {
            return Optional.of(event).filter(DomainEvent::isPre);
        }
        return Optional.empty();
    }

    public <T, ID> Optional<DomainEvent<T, ID>> afterInsert(DomainEvent<T, ID> event) {
        return afterAction(event, DaoAction.INSERT);
    }

    public <T, ID> Optional<DomainEvent<T, ID>> afterUpdate(DomainEvent<T, ID> event) {
        return afterAction(event, DaoAction.UPDATE);
    }

    public <T, ID> Optional<DomainEvent<T, ID>> afterDelete(DomainEvent<T, ID> event) {
        return afterAction(event, DaoAction.DELETE);
    }

    public <T, ID> Optional<DomainEvent<T, ID>> afterLogicDelete(DomainEvent<T, ID> event) {
        return afterAction(event, DaoAction.LOGIC_DELETE);
    }

    public <T, ID> Optional<DomainEvent<T, ID>> afterSelect(DomainEvent<T, ID> event) {
        return afterAction(event, DaoAction.SELECT);
    }

    public <T, ID> Optional<DomainEvent<T, ID>> afterAction(DomainEvent<T, ID> event, DaoAction... action) {
        if (Objects.nonNull(event) && ArrayUtils.contains(action, event.getAction())) {
            return Optional.of(event).filter(DomainEvent::isPost);
        }
        return Optional.empty();
    }

}
