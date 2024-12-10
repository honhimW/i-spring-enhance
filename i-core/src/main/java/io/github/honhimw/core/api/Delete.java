package io.github.honhimw.core.api;

import io.github.honhimw.core.IResult;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author hon_him
 * @since 2024-12-03
 */

public interface Delete<D> {

    @Operation(summary = "delete")
    @PostMapping("/delete")
    IResult<Void> delete(D delete);

}
