package io.github.honhimw.spring.web.common.ddd;

import io.github.honhimw.core.IResult;
import io.github.honhimw.core.IdRequest;
import io.github.honhimw.ddd.jpa.domain.AbstractLogicDeleteAR;
import io.github.honhimw.ddd.jpa.domain.repository.BaseRepository;
import io.github.honhimw.spring.annotation.resolver.TextParam;

/**
 * @author hon_him
 * @since 2023-04-04
 */

public abstract class BaseLogicDeleteImpl<C, U extends IdRequest<ID>, ID, DO extends AbstractLogicDeleteAR<DO, ID>, DTO> extends
    BaseImpl<C, U, ID, DO, DTO> {

    @Override
    protected abstract BaseRepository<DO, ID> getRepository();

    @Override
    public IResult<Void> delete(@TextParam IdRequest<ID> delete) {
        ID id = delete.getId();
        getRepository().logicDelete(id);
        return IResult.ok();
    }

}
