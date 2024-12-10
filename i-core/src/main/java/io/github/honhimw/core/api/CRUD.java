package io.github.honhimw.core.api;

import io.github.honhimw.core.IResult;

/**
 * @param <C> Create entity type
 * @param <R> Query entity type
 * @param <U> Update entity type
 * @param <D> Delete entity type
 * @param <E> Entity
 * @author hon_him
 * @since 2022-07-26
 */
public interface CRUD<C, R, U, D, E> extends Create<C, E>, Read<R, E>, Update<U>, Delete<D>, Paging<E> {

    String SORRY = "sorry, operation not implemented...";

    static <T> IResult<T> saySorry() {
        return IResult.okWithMsg(SORRY);
    }

}
