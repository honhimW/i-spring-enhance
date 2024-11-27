package io.github.honhimw.core;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2022-07-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class IdRequest<T> implements Serializable {

    @NotNull
    @Valid
    private T id;

    public static <T> IdRequest<T> of(T id) {
        return new IdRequest<>(id);
    }

    public <R> IdRequest<R> map(Function<T, R> mapper) {
        R r = mapper.apply(getId());
        return of(r);
    }


}
