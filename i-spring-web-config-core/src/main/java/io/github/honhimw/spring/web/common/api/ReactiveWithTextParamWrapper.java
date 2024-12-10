package io.github.honhimw.spring.web.common.api;

import io.github.honhimw.core.*;
import io.github.honhimw.core.api.DefaultCRUD;
import io.github.honhimw.core.api.ReactiveDefaultCRUD;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * @param <ID> Identity type
 * @author hon_him
 * @since 2022-08-18
 */

public abstract class ReactiveWithTextParamWrapper<C, U, ID, E> implements ReactiveDefaultCRUD<C, U, ID, E> {

    @Nonnull
    protected abstract DefaultCRUD<C, U, ID, E> getDelegate();

    @Nonnull
    protected <R> Mono<R> decorate(@Nonnull Mono<R> mono) {
        return mono.subscribeOn(Schedulers.boundedElastic());
    }

    @Nonnull
    protected <R> Flux<R> decorate(@Nonnull Flux<R> flux) {
        return flux.subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<IResult<E>> create(@TextParam C create) {
        return decorate(Mono.just(create).map(getDelegate()::create));
    }

    @Override
    public Mono<IResult<E>> get(@TextParam IdRequest<ID> read) {
        return decorate(Mono.just(read).map(getDelegate()::get));
    }

    @Override
    public Mono<IResult<Void>> update(@TextParam U update) {
        return decorate(Mono.just(update).map(getDelegate()::update));
    }

    @Override
    public Mono<IResult<Void>> delete(@TextParam IdRequest<ID> delete) {
        return decorate(Mono.just(delete).map(getDelegate()::delete));
    }

    @Override
    public Mono<IResult<PageInfoVO<E>>> list(@TextParam IPageRequest<E> iPageRequest) {
        return decorate(Mono.just(iPageRequest).map(getDelegate()::list));
    }

    @Override
    public Mono<IResult<List<E>>> batchGet(@TextParam BatchIdRequest<ID> read) {
        return decorate(Mono.just(read).map(getDelegate()::batchGet));
    }

    @Override
    public Mono<IResult<Void>> batchDelete(@TextParam BatchIdRequest<ID> delete) {
        return decorate(Mono.just(delete).map(getDelegate()::batchDelete));
    }
}
