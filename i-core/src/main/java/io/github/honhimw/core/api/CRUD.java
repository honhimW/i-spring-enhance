package io.github.honhimw.core.api;

import io.github.honhimw.core.IPageRequest;
import io.github.honhimw.core.PageInfoVO;
import io.github.honhimw.core.IResult;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @param <C> Create entity type
 * @param <R> Query entity type
 * @param <U> Update entity type
 * @param <D> Delete entity type
 * @param <E> Entity
 * @author hon_him
 * @since 2022-07-26
 */
public interface CRUD<C, R, U, D, E> {

    String SORRY = "sorry, operation not implemented...";

    @Operation(summary = "create")
    @PostMapping("/create")
    IResult<E> create(C create);

    @Operation(summary = "query")
    @PostMapping("/get")
    IResult<E> get(R read);

    @Operation(summary = "update")
    @PostMapping("/update")
    IResult<Void> update(U update);

    @Operation(summary = "delete")
    @PostMapping("/delete")
    IResult<Void> delete(D delete);

    @Operation(summary = "paging")
    @PostMapping("/list")
    IResult<PageInfoVO<E>> list(IPageRequest<E> iPageRequest);

    static <T> IResult<T> saySorry() {
        return IResult.okWithMsg(SORRY);
    }

}
