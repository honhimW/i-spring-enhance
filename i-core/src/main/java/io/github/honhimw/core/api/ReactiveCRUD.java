package io.github.honhimw.core.api;

import io.github.honhimw.core.IPageRequest;
import io.github.honhimw.core.IResult;
import io.github.honhimw.core.PageInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

/**
 * @param <C> Create entity type
 * @param <R> Query entity type
 * @param <U> Update entity type
 * @param <D> Delete entity type
 * @param <E> Entity
 * @author hon_him
 * @since 2022-07-26
 */
public interface ReactiveCRUD<C, R, U, D, E> {

    String SORRY = "sorry, operation not implemented...";

    @Operation(summary = "create")
    @PostMapping("/create")
    Mono<IResult<E>> create(C create);

    @Operation(summary = "query")
    @PostMapping("/get")
    Mono<IResult<E>> get(R read);

    @Operation(summary = "update")
    @PostMapping("/update")
    Mono<IResult<Void>> update(U update);

    @Operation(summary = "delete")
    @PostMapping("/delete")
    Mono<IResult<Void>> delete(D delete);

    @Operation(summary = "paging")
    @PostMapping("/list")
    Mono<IResult<PageInfoVO<E>>> list(IPageRequest<E> iPageRequest);

    static <T> Mono<IResult<T>> saySorry() {
        return Mono.just(IResult.okWithMsg(SORRY));
    }

}
