package io.github.honhimw.spring.data.jpa.domain.listener;

import io.github.honhimw.spring.data.common.DaoAction;
import io.github.honhimw.spring.data.common.DomainEntity;
import io.github.honhimw.spring.data.common.LogicDelete;
import jakarta.persistence.PostUpdate;
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
public class LogicDeleteListener {

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostUpdate
    public void postUpdate(Object entity) {
        if (entity instanceof LogicDelete logicDeleteEntity) {
            if (logicDeleteEntity.isDeleted() && entity instanceof DomainEntity<?, ?> domainEntity) {
                Optional.of(domainEntity)
                    .map(DomainEntity::eventBuilder)
                    .map(daoActionFunction -> daoActionFunction.apply(DaoAction.LOGIC_DELETE))
                    .ifPresent(event -> publisher.publishEvent(event));
            }
        }
    }

}
