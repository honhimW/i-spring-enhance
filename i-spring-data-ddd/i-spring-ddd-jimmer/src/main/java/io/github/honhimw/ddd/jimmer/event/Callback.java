package io.github.honhimw.ddd.jimmer.event;

/**
 * @author hon_him
 * @since 2025-03-21
 */

public interface Callback {

    void preCreate(Object entity);

    void postCreate(Object entity);

    boolean preUpdate(Object entity);

    void postUpdate(Object entity);

    void preRemove(Object entity);

    void postRemove(Object entity);

    boolean postLoad(Object entity);

}
