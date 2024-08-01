package io.github.honhimw.spring.web.common.api;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.model.BatchIdRequest;
import io.github.honhimw.spring.model.IPageRequest;
import io.github.honhimw.spring.model.IdRequest;
import io.github.honhimw.spring.model.PageInfoVO;
import io.github.honhimw.spring.data.common.api.ReactiveDefaultCRUD;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author hon_him
 * @since 2023-07-21
 */

public interface ReactiveWithTextParam<C, U, I, E> extends ReactiveDefaultCRUD<C, U, I, E> {

    @Override
    Mono<Result<E>> create(@TextParam C create);

    @Override
    Mono<Result<E>> get(@TextParam IdRequest<I> read);

    @Override
    Mono<Result<Void>> update(@TextParam U update);

    @Override
    Mono<Result<Void>> delete(@TextParam IdRequest<I> delete);

    @Override
    Mono<Result<PageInfoVO<E>>> list(@TextParam IPageRequest<E> iPageRequest);

    @Override
    Mono<Result<List<E>>> batchGet(@TextParam BatchIdRequest<I> read);

    @Override
    Mono<Result<Void>> batchDelete(@TextParam BatchIdRequest<I> delete);
}
