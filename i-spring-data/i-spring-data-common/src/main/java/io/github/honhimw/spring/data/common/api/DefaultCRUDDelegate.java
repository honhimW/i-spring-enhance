package io.github.honhimw.spring.data.common.api;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.model.BatchIdRequest;
import io.github.honhimw.spring.model.IPageRequest;
import io.github.honhimw.spring.model.IdRequest;
import io.github.honhimw.spring.model.PageInfoVO;

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
    public Result<E> create(C create) {
        return _target.create(create);
    }

    @Override
    public Result<E> get(IdRequest<ID> read) {
        return _target.get(read);
    }

    @Override
    public Result<Void> update(U update) {
        return _target.update(update);
    }

    @Override
    public Result<Void> delete(IdRequest<ID> delete) {
        return _target.delete(delete);
    }

    @Override
    public Result<PageInfoVO<E>> list(IPageRequest<E> iPageRequest) {
        return _target.list(iPageRequest);
    }

    @Override
    public Result<List<E>> batchGet(BatchIdRequest<ID> read) {
        return _target.batchGet(read);
    }

    @Override
    public Result<Void> batchDelete(BatchIdRequest<ID> delete) {
        return _target.batchDelete(delete);
    }
}
