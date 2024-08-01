package io.github.honhimw.spring.data.common.api;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.model.BatchIdRequest;
import io.github.honhimw.spring.model.IdRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @param <ID> 主键类型
 * @author hon_him
 * @since 2022-08-18
 */

public interface DefaultCRUD<C, U, ID, E> extends CRUD<C, IdRequest<ID>, U, IdRequest<ID>, E> {

    @Operation(summary = "批量查询")
    @PostMapping("/batchGet")
    Result<List<E>> batchGet(BatchIdRequest<ID> read);

    @Operation(summary = "批量删除", hidden = true)
    @PostMapping("/batchDelete")
    Result<Void> batchDelete(BatchIdRequest<ID> delete);

}
