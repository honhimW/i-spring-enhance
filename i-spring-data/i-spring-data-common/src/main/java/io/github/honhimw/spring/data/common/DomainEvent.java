package io.github.honhimw.spring.data.common;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * {@link ID} 表示的是一个标识, 不一定是某个实体的id
 * {@link T} 表示的是一个结构, 不一定是某个实体对象
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

}
