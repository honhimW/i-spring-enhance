package io.github.honhimw.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author hon_him
 * @since 2022-07-27
 */
@Data
@EqualsAndHashCode
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

}
