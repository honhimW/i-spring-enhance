package io.github.honhimw.spring.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hon_him
 * @since 2022-08-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BatchIdRequest<T> implements Serializable {

    @Size(min = 1, max = 2048)
    @NotNull
    @Valid
    private Set<T> ids;

    public static <T> BatchIdRequest<T> of(Set<T> ids) {
        BatchIdRequest<T> batchIdRequest = new BatchIdRequest<>();
        batchIdRequest.setIds(ids);
        return batchIdRequest;
    }

    public <R> BatchIdRequest<R> map(Function<T, R> mapper) {
        Set<R> collect = ids.stream().map(mapper).collect(Collectors.toSet());
        return of(collect);
    }

}
