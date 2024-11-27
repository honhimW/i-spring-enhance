package io.github.honhimw.core.api;

/**
 * @author hon_him
 * @since 2023-04-04
 */

public interface BaseMapper<C, U, DO, DTO> {

    DO create2do(C request);

    void update2do(U request, DO _do);

    DTO do2dto(DO _do);

    DO dto2do(DTO dto);

}
