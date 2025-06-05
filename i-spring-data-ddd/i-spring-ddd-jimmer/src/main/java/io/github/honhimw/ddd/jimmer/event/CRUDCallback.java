package io.github.honhimw.ddd.jimmer.event;

import io.github.honhimw.ddd.common.*;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.meta.ImmutableType;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

/**
 * @author honhimW
 * @since 2025-06-04
 */

public class CRUDCallback implements Callback {

    private final ApplicationEventPublisher publisher;

    public CRUDCallback(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void preCreate(Object entity) {
        publishPreEvent(entity, DaoAction.INSERT);
    }

    @Override
    public void postCreate(Object entity) {
        publishPostEvent(entity, DaoAction.INSERT);
    }

    @Override
    public void preUpdate(Object entity) {
        if (entity instanceof LogicDelete logicDeleteEntity) {
            if (logicDeleteEntity.isDeleted()) {
                publishPreEvent(entity, DaoAction.LOGIC_DELETE);
                return;
            }
        }
        publishPreEvent(entity, DaoAction.UPDATE);
    }

    @Override
    public void postUpdate(Object entity) {
        if (entity instanceof LogicDelete logicDeleteEntity) {
            if (logicDeleteEntity.isDeleted()) {
                publishPostEvent(entity, DaoAction.LOGIC_DELETE);
                return;
            }
        }
        publishPostEvent(entity, DaoAction.UPDATE);
    }

    @Override
    public void preRemove(Object entity) {
        publishPreEvent(entity, DaoAction.DELETE);
    }

    @Override
    public void postRemove(Object entity) {
        publishPostEvent(entity, DaoAction.DELETE);
    }

    @Override
    public void preSoftRemove(Object entity) {
        publishPreEvent(entity, DaoAction.LOGIC_DELETE);
    }

    @Override
    public void postSoftRemove(Object entity) {
        publishPostEvent(entity, DaoAction.LOGIC_DELETE);
    }

    @Override
    public void postLoad(Object entity) {
        if (entity instanceof SelectEvent) {
            publishPostEvent(entity, DaoAction.SELECT);
        }
    }

    protected void publishPostEvent(Object entity, DaoAction action) {
        publisher.publishEvent(buildEvent(true, entity, action));
    }

    protected void publishPreEvent(Object entity, DaoAction action) {
        publisher.publishEvent(buildEvent(false, entity, action));
    }

    protected DomainEvent<?, ?> buildEvent(boolean post, Object entity, DaoAction action) {
        DomainEvent<?, ?> e;
        if (entity instanceof DomainEntity domainEntity) {
            e = domainEntity.eventBuilder().apply(action);
        } else {
            Class<?> aClass = entity.getClass();
            ImmutableType immutableType = ImmutableType.tryGet(aClass);
            Object id = ImmutableObjects.get(entity, immutableType.getIdProp());
            e = new DomainEvent<>(action).id(id).entity(entity);
        }
        return post ? e.postEvent() : e.preEvent();
    }

}
