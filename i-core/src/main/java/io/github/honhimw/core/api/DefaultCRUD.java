package io.github.honhimw.core.api;

import io.github.honhimw.core.BatchIdRequest;
import io.github.honhimw.core.IdRequest;
import io.github.honhimw.core.IResult;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @param <ID> Identity type
 * @author hon_him
 * @since 2022-08-18
 */

public interface DefaultCRUD<C, U, ID, E> extends CRUD<C, IdRequest<ID>, U, IdRequest<ID>, E> {

    @Operation(summary = "get by id in batch")
    @PostMapping("/batchGet")
    IResult<List<E>> batchGet(BatchIdRequest<ID> read);

    @Operation(summary = "delete by id in batch", hidden = true)
    @PostMapping("/batchDelete")
    IResult<Void> batchDelete(BatchIdRequest<ID> delete);

}
