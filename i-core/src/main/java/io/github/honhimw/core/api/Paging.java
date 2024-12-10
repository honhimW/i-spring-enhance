package io.github.honhimw.core.api;

import io.github.honhimw.core.IPageRequest;
import io.github.honhimw.core.IResult;
import io.github.honhimw.core.PageInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author hon_him
 * @since 2024-12-03
 */

public interface Paging<E> {

    @Operation(summary = "paging")
    @PostMapping("/list")
    IResult<PageInfoVO<E>> list(IPageRequest<E> iPageRequest);

}
