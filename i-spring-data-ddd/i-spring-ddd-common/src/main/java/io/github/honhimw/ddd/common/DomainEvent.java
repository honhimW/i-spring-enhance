package io.github.honhimw.ddd.common;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author hon_him
 * @since 2022-10-21
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DomainEvent<T, ID> implements Serializable {

    @NotNull
    private final DaoAction action;

    private ID id;

    private T entity;

    private boolean pre = false;

    public DomainEvent(DaoAction action) {
        this.action = action;
    }

    public DomainEvent<T, ID> id(ID id) {
        this.setId(id);
        return this;
    }

    public DomainEvent<T, ID> entity(T entity) {
        this.setEntity(entity);
        return this;
    }

    public final DomainEvent<T, ID> preEvent() {
        this.pre = true;
        return this;
    }

    public final DomainEvent<T, ID> postEvent() {
        this.pre = false;
        return this;
    }

    public boolean isPost() {
        return !this.pre;
    }

}
