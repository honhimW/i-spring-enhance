package io.github.honhimw.spring.web.common.api;

import io.github.honhimw.core.BatchIdRequest;
import io.github.honhimw.core.IPageRequest;
import io.github.honhimw.core.IResult;
import io.github.honhimw.core.IdRequest;
import io.github.honhimw.core.PageInfoVO;
import io.github.honhimw.core.api.DefaultCRUD;
import io.github.honhimw.spring.annotation.resolver.TextParam;

import java.util.List;

/**
 * @param <I> Identity type
 * @author hon_him
 * @since 2022-08-18
 */

public interface WithTextParam<C, U, I, E> extends DefaultCRUD<C, U, I, E> {

    @Override
    IResult<E> create(@TextParam C create);

    @Override
    IResult<E> get(@TextParam IdRequest<I> read);

    @Override
    IResult<Void> update(@TextParam U update);

    @Override
    IResult<Void> delete(@TextParam IdRequest<I> delete);

    @Override
    IResult<PageInfoVO<E>> list(@TextParam IPageRequest<E> iPageRequest);

    @Override
    IResult<List<E>> batchGet(@TextParam BatchIdRequest<I> read);

    @Override
    IResult<Void> batchDelete(@TextParam BatchIdRequest<I> delete);
}
