package io.github.honhimw.spring.web.common.ddd;

import io.github.honhimw.core.*;
import io.github.honhimw.core.api.BaseMapper;
import io.github.honhimw.core.api.DefaultCRUD;
import io.github.honhimw.ddd.jpa.domain.AbstractAR;
import io.github.honhimw.ddd.jpa.domain.repository.SimpleRepository;
import io.github.honhimw.ddd.jpa.util.PageUtils;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    public IResult<DTO> create(@TextParam C create) {
        DO _do = this.getMapper().create2do(create);
        this.beforeCreate(create, _do);
        this.beforeSave(_do);
        _do = this.getRepository().save(_do);
        DTO dto = this.getMapper().do2dto(_do);
        return IResult.ok(dto);
    }

    @Override
    public IResult<DTO> get(@TextParam IdRequest<ID> read) {
        ID id = read.getId();
        DO aDo = getRepository().findById(id).orElseThrow(() -> new NoSuchElementException("[%s] not found".formatted(id)));
        DTO dto = getMapper().do2dto(aDo);
        return IResult.ok(dto);
    }

    @Override
    public IResult<Void> update(@TextParam U update) {
        getRepository().update(update.getId(), _do -> {
            this.beforeUpdate(update, _do);
            getMapper().update2do(update, _do);
            this.beforeSave(_do);
        });
        return IResult.ok();
    }

    @Override
    public IResult<Void> delete(@TextParam IdRequest<ID> delete) {
        ID id = delete.getId();
        getRepository().deleteById(id);
        return IResult.ok();
    }

    @Override
    public IResult<PageInfoVO<DTO>> list(@TextParam IPageRequest<DTO> iPageRequest) {
        if (iPageRequest.getCondition() instanceof Map<?, ?> map) {
            List<ConditionColumn> conditions = iPageRequest.getConditions();
            final List<ConditionColumn> finalConditions;
            if (CollectionUtils.isEmpty(conditions)) {
                finalConditions = new ArrayList<>();
            } else {
                finalConditions = new ArrayList<>(conditions);
            }
            map.forEach((key, value) -> finalConditions.add(ConditionColumn.of(String.valueOf(key), value)));
            iPageRequest.setCondition(null);
            iPageRequest.setConditions(finalConditions);
        }
        IPageRequest<DO> request = PageUtils.convertRequest(iPageRequest, getMapper()::dto2do);
        Page<DO> dos = PageUtils.paging(getRepository(), request);
        PageInfoVO<DTO> vo = PageUtils.pageInfoVO(dos, getMapper()::do2dto);
        return IResult.ok(vo);
    }

    @Override
    public IResult<List<DTO>> batchGet(@TextParam BatchIdRequest<ID> read) {
        Set<ID> ids = read.getIds();
        List<DO> dos = getRepository().findAllById(ids);
        List<DTO> dtos = dos.stream().map(getMapper()::do2dto).toList();
        return IResult.ok(dtos);
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public IResult<Void> batchDelete(BatchIdRequest<ID> delete) {
        Set<ID> ids = delete.getIds();
        for (ID id : ids) {
            delete(IdRequest.of(id));
        }
        return IResult.ok();
    }
}
