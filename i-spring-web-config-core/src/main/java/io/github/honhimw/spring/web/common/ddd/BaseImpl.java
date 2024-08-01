package io.github.honhimw.spring.web.common.ddd;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.data.common.*;
import io.github.honhimw.spring.data.common.api.DefaultCRUD;
import io.github.honhimw.spring.data.jpa.domain.AbstractAR;
import io.github.honhimw.spring.data.jpa.domain.repository.SimpleRepository;
import io.github.honhimw.spring.data.jpa.util.PageUtils;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.spring.WrappedException;
import io.github.honhimw.spring.model.BatchIdRequest;
import io.github.honhimw.spring.model.IPageRequest;
import io.github.honhimw.spring.model.IdRequest;
import io.github.honhimw.spring.model.PageInfoVO;
import io.github.honhimw.spring.util.Brook;
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

public abstract class BaseImpl<C, U extends IdRequest<ID>, ID, DO extends AbstractAR<DO, ID>, DTO> implements DefaultCRUD<C, U, ID, DTO> {

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
    public Result<DTO> create(@TextParam C create) {
        return Brook.with(create)
            .map(getMapper()::create2do)
            .exec(_do -> this.beforeCreate(create, _do))
            .exec(this::beforeSave)
            .map(getRepository()::save)
            .map(getMapper()::do2dto)
            .map(Result::ok)
            .errElseThrow(WrappedException::new);
    }

    @Override
    public Result<DTO> get(@TextParam IdRequest<ID> read) {
        return Brook.with(read)
            .map(IdRequest::getId)
            .flatOptional(getRepository()::findById)
            .map(getMapper()::do2dto)
            .map(Result::ok)
            .errElseThrow(WrappedException::new);
    }

    @Override
    public Result<Void> update(@TextParam U update) {
        return Brook.with(update)
            .map(r -> getRepository().update(r.getId(), _do -> {
                this.beforeUpdate(r, _do);
                getMapper().update2do(r, _do);
                this.beforeSave(_do);
            }))
            .map(_do -> Result.<Void>ok())
            .errElseThrow(WrappedException::new);
    }

    @Override
    public Result<Void> delete(@TextParam IdRequest<ID> delete) {
        return Brook.with(delete)
            .map(IdRequest::getId)
            .exec(getRepository()::deleteById)
            .map(_do -> Result.<Void>ok())
            .errElseThrow(WrappedException::new);
    }

    @Override
    public Result<PageInfoVO<DTO>> list(@TextParam IPageRequest<DTO> iPageRequest) {
        return Brook.with(iPageRequest)
            .exec(request -> {
                if (request.getCondition() instanceof Map<?, ?> map) {
                    List<IPageRequest.ConditionColumn> conditions = request.getConditions();
                    final List<IPageRequest.ConditionColumn> finalConditions;
                    if (CollectionUtils.isEmpty(conditions)) {
                        finalConditions = new ArrayList<>();
                    } else {
                        finalConditions = new ArrayList<>(conditions);
                    }
                    map.forEach((key, value) -> finalConditions.add(IPageRequest.ConditionColumn.of(String.valueOf(key), value)));
                    request.setCondition(null);
                    request.setConditions(finalConditions);
                }
            })
            .map(request -> PageUtils.convertRequest(request, getMapper()::dto2do))
            .map(request -> PageUtils.paging(getRepository(), request))
            .map(dos -> PageUtils.pageInfoVO(dos, getMapper()::do2dto))
            .map(Result::ok)
            .errElseThrow(WrappedException::new);
    }

    @Override
    public Result<List<DTO>> batchGet(@TextParam BatchIdRequest<ID> read) {
        return Brook.with(read)
            .map(BatchIdRequest::getIds)
            .map(getRepository()::findAllById)
            .map(dos -> dos.stream().map(getMapper()::do2dto).toList())
            .map(Result::ok)
            .errElseThrow(WrappedException::new);
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Result<Void> batchDelete(BatchIdRequest<ID> delete) {
        Set<ID> ids = delete.getIds();
        for (ID id : ids) {
            delete(IdRequest.of(id));
        }
        return Result.ok();
    }
}
