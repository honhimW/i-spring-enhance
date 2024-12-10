package io.github.honhimw.spring.web.common.api;

import io.github.honhimw.core.*;
import io.github.honhimw.core.api.ReactiveDefaultCRUD;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author hon_him
 * @since 2023-07-21
 */

public interface ReactiveWithTextParam<C, U, I, E> extends ReactiveDefaultCRUD<C, U, I, E> {

    @Override
    Mono<IResult<E>> create(@TextParam C create);

    @Override
    Mono<IResult<E>> get(@TextParam IdRequest<I> read);

    @Override
    Mono<IResult<Void>> update(@TextParam U update);

    @Override
    Mono<IResult<Void>> delete(@TextParam IdRequest<I> delete);

    @Override
    Mono<IResult<PageInfoVO<E>>> list(@TextParam IPageRequest<E> iPageRequest);

    @Override
    Mono<IResult<List<E>>> batchGet(@TextParam BatchIdRequest<I> read);

    @Override
    Mono<IResult<Void>> batchDelete(@TextParam BatchIdRequest<I> delete);
}
