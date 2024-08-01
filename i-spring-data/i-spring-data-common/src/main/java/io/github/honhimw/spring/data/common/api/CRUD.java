package io.github.honhimw.spring.data.common.api;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.model.IPageRequest;
import io.github.honhimw.spring.model.PageInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @param <C> 新增参数
 * @param <R> 查询参数
 * @param <U> 更新参数
 * @param <D> 删除参数
 * @param <E> 实体
 * @author hon_him
 * @since 2022-07-26
 */
public interface CRUD<C, R, U, D, E> {

    String SORRY = "sorry, operation not implemented...";

    @Operation(summary = "新建")
    @PostMapping("/create")
    Result<E> create(C create);

    @Operation(summary = "查询")
    @PostMapping("/get")
    Result<E> get(R read);

    @Operation(summary = "更新")
    @PostMapping("/update")
    Result<Void> update(U update);

    @Operation(summary = "删除")
    @PostMapping("/delete")
    Result<Void> delete(D delete);

    @Operation(summary = "分页")
    @PostMapping("/list")
    Result<PageInfoVO<E>> list(IPageRequest<E> iPageRequest);

    static <T> Result<T> saySorry() {
        return Result.okWithMsg(SORRY);
    }

}
