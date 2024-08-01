package io.github.honhimw.spring.web.common.api;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.model.BatchIdRequest;
import io.github.honhimw.spring.model.IPageRequest;
import io.github.honhimw.spring.model.IdRequest;
import io.github.honhimw.spring.model.PageInfoVO;
import io.github.honhimw.spring.data.common.api.DefaultCRUD;
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
    @Operation(summary = "批量查询")
    public Result<List<E>> batchGet(@TextParam BatchIdRequest<I> read) {
        return getDelegate().batchGet(read);
    }

    @Override
    @PostMapping({"/batchDelete"})
    @Operation(summary = "批量删除", hidden = true)
    public Result<Void> batchDelete(@TextParam BatchIdRequest<I> delete) {
        return getDelegate().batchDelete(delete);
    }

    @Override
    @PostMapping({"/create"})
    @Operation(summary = "新建")
    public Result<E> create(@TextParam C create) {
        return getDelegate().create(create);
    }

    @Override
    @PostMapping({"/get"})
    @Operation(summary = "查询")
    public Result<E> get(@TextParam IdRequest<I> read) {
        return getDelegate().get(read);
    }

    @Override
    @PostMapping({"/update"})
    @Operation(summary = "更新")
    public Result<Void> update(@TextParam U update) {
        return getDelegate().update(update);
    }

    @Override
    @PostMapping({"/delete"})
    @Operation(summary = "删除")
    public Result<Void> delete(@TextParam IdRequest<I> delete) {
        return getDelegate().delete(delete);
    }

    @Override
    @PostMapping({"/list"})
    @Operation(summary = "分页")
    public Result<PageInfoVO<E>> list(@TextParam IPageRequest<E> iPageRequest) {
        return getDelegate().list(iPageRequest);
    }
}
