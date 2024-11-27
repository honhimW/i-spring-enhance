package io.github.honhimw.ddd.jpa.domain;

import io.github.honhimw.ddd.common.DomainEntity;
import io.github.honhimw.ddd.jpa.domain.listener.CRUDListener;
import io.github.honhimw.spring.ValidatorUtils;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.domain.AbstractAggregateRoot;

/**
 * @author hon_him
 * @since 2022-10-17
 */
@MappedSuperclass
@EntityListeners(CRUDListener.class)
public abstract class AbstractAR<A extends AbstractAR<A, ID>, ID> extends AbstractAggregateRoot<A> implements
    DomainEntity<A, ID> {

    public void validate() {
        ValidatorUtils.validate(this);
    }

    /**
     * Select event is disabled by default
     */
    protected boolean enableSelectEvent() {
        return false;
    }

}
