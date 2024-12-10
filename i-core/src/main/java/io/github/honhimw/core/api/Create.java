package io.github.honhimw.core.api;

import io.github.honhimw.core.IResult;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author hon_him
 * @since 2024-12-03
 */

public interface Create<C, E> {

    @Operation(summary = "create")
    @PostMapping("/create")
    IResult<E> create(C create);

}
