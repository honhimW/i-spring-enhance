package io.github.honhimw.core.api;

import io.github.honhimw.core.BatchIdRequest;
import io.github.honhimw.core.IdRequest;
import io.github.honhimw.core.IResult;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @param <I> Identity type
 * @author hon_him
 * @since 2022-08-18
 */

public interface ReactiveDefaultCRUD<C, U, I, E> extends ReactiveCRUD<C, IdRequest<I>, U, IdRequest<I>, E> {

    @Operation(summary = "get by id in batch")
    @PostMapping("/batchGet")
    Mono<IResult<List<E>>> batchGet(BatchIdRequest<I> read);

    @Operation(summary = "delete by id in batch", hidden = true)
    @PostMapping("/batchDelete")
    Mono<IResult<Void>> batchDelete(BatchIdRequest<I> delete);

}
