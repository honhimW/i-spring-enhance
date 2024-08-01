package io.github.honhimw.spring.data.jpa.domain;

import io.github.honhimw.spring.data.common.DomainEntity;
import io.github.honhimw.spring.data.common.ValidatorUtils;
import io.github.honhimw.spring.data.jpa.domain.listener.CRUDListener;
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
     * 默认不开启读事件
     */
    protected boolean enableSelectEvent() {
        return false;
    }

}
