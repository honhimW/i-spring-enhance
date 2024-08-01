package io.github.honhimw.spring.data.common.api;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.model.BatchIdRequest;
import io.github.honhimw.spring.model.IdRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @param <I> 主键类型
 * @author hon_him
 * @since 2022-08-18
 */

public interface ReactiveDefaultCRUD<C, U, I, E> extends ReactiveCRUD<C, IdRequest<I>, U, IdRequest<I>, E> {

    @Operation(summary = "批量查询")
    @PostMapping("/batchGet")
    Mono<Result<List<E>>> batchGet(BatchIdRequest<I> read);

    @Operation(summary = "批量删除", hidden = true)
    @PostMapping("/batchDelete")
    Mono<Result<Void>> batchDelete(BatchIdRequest<I> delete);

}
