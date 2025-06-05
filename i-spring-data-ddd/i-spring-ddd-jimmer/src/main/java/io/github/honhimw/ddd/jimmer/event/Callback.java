package io.github.honhimw.ddd.jimmer.event;

/**
 * @author hon_him
 * @since 2025-03-21
 */

public interface Callback {

    default void preCreate(Object entity) {

    }

    default void postCreate(Object entity) {

    }

    default void preUpdate(Object entity) {

    }

    default void postUpdate(Object entity) {

    }

    default void preRemove(Object entity) {

    }

    default void postRemove(Object entity) {

    }

    default void preSoftRemove(Object entity) {

    }

    default void postSoftRemove(Object entity) {

    }

    default void postLoad(Object entity) {

    }

}
