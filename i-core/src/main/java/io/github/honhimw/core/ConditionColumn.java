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
public class ConditionColumn implements Serializable {

    @NotBlank
    private String name;

    private Object value;

    private MatchingType type = MatchingType.EQUAL;

    private String group = "root";

    public static ConditionColumn of(String name, Object value) {
        return of(name, value, MatchingType.EQUAL);
    }

    public static ConditionColumn of(String name, Object value, MatchingType type) {
        ConditionColumn column = new ConditionColumn();
        column.setName(name);
        column.setValue(value);
        column.setType(type);
        return column;
    }

}
