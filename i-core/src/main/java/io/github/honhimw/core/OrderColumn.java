package io.github.honhimw.core;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hon_him
 * @since 2024-11-18
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class OrderColumn implements Serializable {

    public static final String RANDOM_ORDER = "randomOrderSpec";

    @NotBlank
    private String name;

    private Boolean desc = false;

    public static OrderColumn of(String name) {
        return of(name, false);
    }

    public static OrderColumn of(String name, Boolean desc) {
        OrderColumn column = new OrderColumn();
        column.setName(name);
        column.setDesc(desc);
        return column;
    }

}
