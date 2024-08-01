package io.github.honhimw.spring.data.jpa.domain.listener;

import io.github.honhimw.spring.data.common.DaoAction;
import io.github.honhimw.spring.data.common.DomainEntity;
import io.github.honhimw.spring.data.common.LogicDelete;
import io.github.honhimw.spring.data.common.SelectEvent;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author hon_him
 * @since 2022-10-27
 */

@SuppressWarnings("all")
@Transactional
public class CRUDListener {

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostPersist
    public void postPersist(Object entity) {
        if (entity instanceof DomainEntity<?, ?> domainEntity) {
            publish(domainEntity, DaoAction.INSERT);
        }
    }

    @PostLoad
    public void postLoad(Object entity) {
        if (entity instanceof SelectEvent && entity instanceof DomainEntity<?, ?> domainEntity) {
            publish(domainEntity, DaoAction.SELECT);
        }
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        if (entity instanceof DomainEntity<?, ?> domainEntity) {
            if (entity instanceof LogicDelete logicDeleteEntity) {
                if (logicDeleteEntity.isDeleted()) {
                    publish(domainEntity, DaoAction.LOGIC_DELETE);
                    return;
                }
            }
            publish(domainEntity, DaoAction.UPDATE);
        }
    }

    @PostRemove
    public void postRemove(Object entity) {
        if (entity instanceof DomainEntity<?, ?> domainEntity) {
            publish(domainEntity, DaoAction.DELETE);
        }
    }

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof DomainEntity<?, ?> domainEntity) {
            publishPreEvent(domainEntity, DaoAction.INSERT);
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof DomainEntity<?, ?> domainEntity) {
            if (entity instanceof LogicDelete logicDeleteEntity) {
                if (logicDeleteEntity.isDeleted()) {
                    publishPreEvent(domainEntity, DaoAction.LOGIC_DELETE);
                    return;
                }
            }
            publishPreEvent(domainEntity, DaoAction.UPDATE);
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        if (entity instanceof DomainEntity<?, ?> domainEntity) {
            publishPreEvent(domainEntity, DaoAction.DELETE);
        }
    }

    protected void publish(DomainEntity<?, ?> domainEntity, DaoAction action) {
        Optional.of(domainEntity)
            .map(DomainEntity::eventBuilder)
            .map(daoActionFunction -> daoActionFunction.apply(action))
            .ifPresent(event -> publisher.publishEvent(event));
    }

    protected void publishPreEvent(DomainEntity<?, ?> domainEntity, DaoAction action) {
        Optional.of(domainEntity)
            .map(DomainEntity::eventBuilder)
            .map(daoActionFunction -> daoActionFunction.apply(action))
            .map(domainEvent -> domainEvent.preEvent())
            .ifPresent(event -> publisher.publishEvent(event));
    }

}
