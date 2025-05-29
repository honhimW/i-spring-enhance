package io.github.honhimw.core;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hon_him
 * @since 2024-11-18
 */
@Data
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

    public static OrderColumn asc(String name) {
        return of(name, false);
    }

    public static OrderColumn desc(String name) {
        return of(name, true);
    }

    public OrderColumn asc() {
        this.desc = false;
        return this;
    }

    public OrderColumn desc() {
        this.desc = true;
        return this;
    }

}
