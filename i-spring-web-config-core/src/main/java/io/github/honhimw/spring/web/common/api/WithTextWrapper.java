package io.github.honhimw.spring.web.common.api;

import io.github.honhimw.core.*;
import io.github.honhimw.core.api.DefaultCRUD;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author hon_him
 * @since 2023-07-26
 */

public abstract class WithTextWrapper<C, U, I, E> implements DefaultCRUD<C, U, I, E> {

    protected abstract DefaultCRUD<C, U, I, E> getDelegate();

    @Override
    @PostMapping({"/batchGet"})
    @Operation(summary = "get by id in batch")
    public IResult<List<E>> batchGet(@TextParam BatchIdRequest<I> read) {
        return getDelegate().batchGet(read);
    }

    @Override
    @PostMapping({"/batchDelete"})
    @Operation(summary = "delete by id in batch", hidden = true)
    public IResult<Void> batchDelete(@TextParam BatchIdRequest<I> delete) {
        return getDelegate().batchDelete(delete);
    }

    @Override
    @PostMapping({"/create"})
    @Operation(summary = "create")
    public IResult<E> create(@TextParam C create) {
        return getDelegate().create(create);
    }

    @PostMapping({"/get"})
    @Operation(summary = "query")
    public IResult<E> get() {
        return get(null);
    }

    @Override
    @PostMapping({"/get"})
    @Operation(summary = "query")
    public IResult<E> get(@TextParam IdRequest<I> read) {
        return getDelegate().get(read);
    }

    @Override
    @PostMapping({"/update"})
    @Operation(summary = "update")
    public IResult<Void> update(@TextParam U update) {
        return getDelegate().update(update);
    }

    @Override
    @PostMapping({"/delete"})
    @Operation(summary = "delete")
    public IResult<Void> delete(@TextParam IdRequest<I> delete) {
        return getDelegate().delete(delete);
    }

    @Override
    @PostMapping({"/list"})
    @Operation(summary = "paging")
    public IResult<PageInfoVO<E>> list(@TextParam IPageRequest<E> iPageRequest) {
        return getDelegate().list(iPageRequest);
    }
}
