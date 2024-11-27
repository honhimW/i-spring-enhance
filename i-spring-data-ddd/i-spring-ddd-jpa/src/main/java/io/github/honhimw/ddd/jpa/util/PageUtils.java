package io.github.honhimw.ddd.jpa.util;

import io.github.honhimw.core.IPageRequest;
import io.github.honhimw.core.OrderColumn;
import io.github.honhimw.core.PageInfoVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2022-11-18
 */
@SuppressWarnings("unused")
public class PageUtils {

    private static int FIRST_PAGE_NO = 0;

    public static void setFirstPageNo(int firstPageNo) {
        FIRST_PAGE_NO = firstPageNo;
    }

    public static <T> Specification<T> spec(IPageRequest<T> iPageRequest) {
        return new IExampleSpecification<>(iPageRequest);
    }

    public static <T> PageRequest page(IPageRequest<T> iPageRequest) {
        PageRequest pr = PageRequest.of(springPageNo(iPageRequest.getPageNo()), iPageRequest.getPageSize());
        List<OrderColumn> orders = iPageRequest.getOrders();
        if (CollectionUtils.isNotEmpty(orders)) {
            List<Order> os = orders.stream().map(orderColumn -> {
                String name = orderColumn.getName();
                Boolean desc = orderColumn.getDesc();
                if (BooleanUtils.isTrue(desc)) {
                    return Order.desc(name);
                } else {
                    return Order.asc(name);
                }
            }).toList();
            pr = pr.withSort(Sort.by(os));
        }
        return pr;
    }

    public static <T> Page<T> paging(JpaSpecificationExecutor<T> repository, IPageRequest<T> iPageRequest) {
        return repository.findAll(spec(iPageRequest), page(iPageRequest));
    }

    public static <T, R> IPageRequest<R> convertRequest(IPageRequest<T> iPageRequest, Function<T, R> converter) {
        IPageRequest<R> another = new IPageRequest<>();
        another.setPageNo(iPageRequest.getPageNo());
        another.setPageSize(iPageRequest.getPageSize());
        another.setConditions(iPageRequest.getConditions());
        another.setOrders(iPageRequest.getOrders());
        another.setMatchAll(iPageRequest.getMatchAll());
        another.setCondition(converter.apply(iPageRequest.getCondition()));
        return another;
    }

    public static <T, R> Page<R> convertPage(Page<T> page, Function<T, R> mapper) {
        List<R> rs = page.stream().map(mapper).toList();
        return new PageImpl<>(rs, page.getPageable(), page.getTotalElements());
    }

    public static <T, R> PageInfoVO<R> pageInfoVO(Page<T> page, Function<T, R> mapper) {
        List<R> rs = page.stream().map(mapper).toList();
        PageInfoVO<R> pageInfoDTO = new PageInfoVO<>();
        pageInfoDTO.setPages(page.getTotalPages());
        pageInfoDTO.setTotal(page.getTotalElements());
        pageInfoDTO.setList(rs);
        pageInfoDTO.setPageNum(originPageNo(page.getNumber()));
        pageInfoDTO.setPageSize(page.getSize());
        pageInfoDTO.setPages(page.getTotalPages());
        pageInfoDTO.setSize(page.getNumberOfElements());
        return pageInfoDTO;
    }


    private static int springPageNo(int pageNo) {
        return Math.max(pageNo - FIRST_PAGE_NO, 0);
    }

    private static int originPageNo(int pageNo) {
        return pageNo + FIRST_PAGE_NO;
    }

}
