package io.github.honhimw.spring.web.common.ddd;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.model.IdRequest;
import io.github.honhimw.spring.data.jpa.domain.AbstractLogicDeleteAR;
import io.github.honhimw.spring.data.jpa.domain.repository.BaseRepository;
import io.github.honhimw.spring.WrappedException;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.spring.util.Brook;

/**
 * @author hon_him
 * @since 2023-04-04
 */

public abstract class BaseLogicDeleteImpl<C, U extends IdRequest<ID>, ID, DO extends AbstractLogicDeleteAR<DO, ID>, DTO> extends
    BaseImpl<C, U, ID, DO, DTO> {

    @Override
    protected abstract BaseRepository<DO, ID> getRepository();

    @Override
    public Result<Void> delete(@TextParam IdRequest<ID> delete) {
        return Brook.with(delete)
            .map(IdRequest::getId)
            .exec(getRepository()::logicDelete)
            .map(_do -> Result.<Void>ok())
            .errElseThrow(WrappedException::new);
    }

}
