package io.github.honhimw.spring.web.common.api;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.model.BatchIdRequest;
import io.github.honhimw.spring.model.IPageRequest;
import io.github.honhimw.spring.model.IdRequest;
import io.github.honhimw.spring.model.PageInfoVO;
import io.github.honhimw.spring.data.common.api.DefaultCRUD;
import io.github.honhimw.spring.annotation.resolver.TextParam;

import java.util.List;

/**
 * @param <I> 主键类型
 * @author hon_him
 * @since 2022-08-18
 */

public interface WithTextParam<C, U, I, E> extends DefaultCRUD<C, U, I, E> {

    @Override
    Result<E> create(@TextParam C create);

    @Override
    Result<E> get(@TextParam IdRequest<I> read);

    @Override
    Result<Void> update(@TextParam U update);

    @Override
    Result<Void> delete(@TextParam IdRequest<I> delete);

    @Override
    Result<PageInfoVO<E>> list(@TextParam IPageRequest<E> iPageRequest);

    @Override
    Result<List<E>> batchGet(@TextParam BatchIdRequest<I> read);

    @Override
    Result<Void> batchDelete(@TextParam BatchIdRequest<I> delete);
}
