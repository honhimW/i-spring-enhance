package io.github.honhimw.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2022-07-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageInfoVO<T> implements Serializable {

    private Integer pageNum;
    private Integer pageSize;
    private Integer size;
    private Integer pages;
    private List<T> list;
    private Long total;

    public static <T> PageInfoVO<T> empty() {
        PageInfoVO<T> pageInfo = new PageInfoVO<>();
        pageInfo.setPageNum(0);
        pageInfo.setPageSize(0);
        pageInfo.setSize(0);
        pageInfo.setPages(0);
        pageInfo.setTotal(0L);
        pageInfo.setList(Collections.emptyList());
        return pageInfo;
    }

    public <R> PageInfoVO<R> map(Function<T, R> mapper) {
        PageInfoVO<R> pageInfo = new PageInfoVO<>();
        pageInfo.setPageNum(getPageNum());
        pageInfo.setPageSize(getPageSize());
        pageInfo.setSize(getSize());
        pageInfo.setPages(getPages());
        pageInfo.setTotal(getTotal());
        pageInfo.setList(getList().stream().map(mapper).toList());
        return pageInfo;
    }

}
