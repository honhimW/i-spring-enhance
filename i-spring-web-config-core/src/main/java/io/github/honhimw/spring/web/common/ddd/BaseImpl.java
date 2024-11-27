package io.github.honhimw.spring.web.common.ddd;

import io.github.honhimw.core.ConditionColumn;
import io.github.honhimw.core.IResult;
import io.github.honhimw.core.PageInfoVO;
import io.github.honhimw.core.WrappedException;
import io.github.honhimw.core.api.BaseMapper;
import io.github.honhimw.core.api.DefaultCRUD;
import io.github.honhimw.ddd.jpa.domain.AbstractAR;
import io.github.honhimw.ddd.jpa.domain.repository.SimpleRepository;
import io.github.honhimw.ddd.jpa.util.PageUtils;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.util.tool.Brook;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hon_him
 * @since 2023-04-04
 */

public abstract class BaseImpl<C, U extends io.github.honhimw.core.IdRequest<ID>, ID, DO extends AbstractAR<DO, ID>, DTO> implements DefaultCRUD<C, U, ID, DTO> {

    protected abstract SimpleRepository<DO, ID> getRepository();

    protected abstract BaseMapper<C, U, DO, DTO> getMapper();

    protected void beforeCreate(C create, DO _do) {
        // do nothing
    }

    protected void beforeUpdate(U update, DO _do) {
        // do nothing
    }

    protected void beforeSave(DO _do) {

    }

    @Override
    public io.github.honhimw.core.IResult<DTO> create(@TextParam C create) {
        return Brook.with(create)
            .map(getMapper()::create2do)
            .exec(_do -> this.beforeCreate(create, _do))
            .exec(this::beforeSave)
            .map(getRepository()::save)
            .map(getMapper()::do2dto)
            .map(io.github.honhimw.core.IResult::ok)
            .errElseThrow(WrappedException::new);
    }

    @Override
    public io.github.honhimw.core.IResult<DTO> get(@TextParam io.github.honhimw.core.IdRequest<ID> read) {
        return Brook.with(read)
            .map(io.github.honhimw.core.IdRequest::getId)
            .flatOptional(getRepository()::findById)
            .map(getMapper()::do2dto)
            .map(io.github.honhimw.core.IResult::ok)
            .errElseThrow(WrappedException::new);
    }

    @Override
    public IResult<Void> update(@TextParam U update) {
        return Brook.with(update)
            .map(r -> getRepository().update(r.getId(), _do -> {
                this.beforeUpdate(r, _do);
                getMapper().update2do(r, _do);
                this.beforeSave(_do);
            }))
            .map(_do -> io.github.honhimw.core.IResult.<Void>ok())
            .errElseThrow(WrappedException::new);
    }

    @Override
    public IResult<Void> delete(@TextParam io.github.honhimw.core.IdRequest<ID> delete) {
        return Brook.with(delete)
            .map(io.github.honhimw.core.IdRequest::getId)
            .exec(getRepository()::deleteById)
            .map(_do -> io.github.honhimw.core.IResult.<Void>ok())
            .errElseThrow(WrappedException::new);
    }

    @Override
    public IResult<PageInfoVO<DTO>> list(@TextParam io.github.honhimw.core.IPageRequest<DTO> iPageRequest) {
        return Brook.with(iPageRequest)
            .exec(request -> {
                if (request.getCondition() instanceof Map<?, ?> map) {
                    List<ConditionColumn> conditions = request.getConditions();
                    final List<ConditionColumn> finalConditions;
                    if (CollectionUtils.isEmpty(conditions)) {
                        finalConditions = new ArrayList<>();
                    } else {
                        finalConditions = new ArrayList<>(conditions);
                    }
                    map.forEach((key, value) -> finalConditions.add(ConditionColumn.of(String.valueOf(key), value)));
                    request.setCondition(null);
                    request.setConditions(finalConditions);
                }
            })
            .map(request -> PageUtils.convertRequest(request, getMapper()::dto2do))
            .map(request -> PageUtils.paging(getRepository(), request))
            .map(dos -> PageUtils.pageInfoVO(dos, getMapper()::do2dto))
            .map(io.github.honhimw.core.IResult::ok)
            .errElseThrow(WrappedException::new);
    }

    @Override
    public io.github.honhimw.core.IResult<List<DTO>> batchGet(@TextParam io.github.honhimw.core.BatchIdRequest<ID> read) {
        return Brook.with(read)
            .map(io.github.honhimw.core.BatchIdRequest::getIds)
            .map(getRepository()::findAllById)
            .map(dos -> dos.stream().map(getMapper()::do2dto).toList())
            .map(io.github.honhimw.core.IResult::ok)
            .errElseThrow(WrappedException::new);
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public IResult<Void> batchDelete(io.github.honhimw.core.BatchIdRequest<ID> delete) {
        Set<ID> ids = delete.getIds();
        for (ID id : ids) {
            delete(io.github.honhimw.core.IdRequest.of(id));
        }
        return io.github.honhimw.core.IResult.ok();
    }
}
