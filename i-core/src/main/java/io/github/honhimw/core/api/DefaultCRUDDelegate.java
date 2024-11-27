package io.github.honhimw.core.api;

import io.github.honhimw.core.*;

import java.util.List;

/**
 * @author hon_him
 * @since 2023-04-25
 */

public abstract class DefaultCRUDDelegate<C, U, ID, E> implements DefaultCRUD<C, U, ID, E> {

    private final DefaultCRUD<C, U, ID, E> _target;

    public DefaultCRUDDelegate(DefaultCRUD<C, U, ID, E> _target) {
        this._target = _target;
    }

    @Override
    public IResult<E> create(C create) {
        return _target.create(create);
    }

    @Override
    public IResult<E> get(IdRequest<ID> read) {
        return _target.get(read);
    }

    @Override
    public IResult<Void> update(U update) {
        return _target.update(update);
    }

    @Override
    public IResult<Void> delete(IdRequest<ID> delete) {
        return _target.delete(delete);
    }

    @Override
    public IResult<PageInfoVO<E>> list(IPageRequest<E> iPageRequest) {
        return _target.list(iPageRequest);
    }

    @Override
    public IResult<List<E>> batchGet(BatchIdRequest<ID> read) {
        return _target.batchGet(read);
    }

    @Override
    public IResult<Void> batchDelete(BatchIdRequest<ID> delete) {
        return _target.batchDelete(delete);
    }
}
