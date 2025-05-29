package io.github.honhimw.core;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author hon_him
 * @since 2024-11-18
 */

@Data
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<ConditionColumn> columns;

        private Builder() {
            this.columns = new ArrayList<>();
        }

        public Builder eq(String name, Object value) {
            this.columns.add(ConditionColumn.of(name, value, MatchingType.EQUAL));
            return this;
        }

        public Builder ne(String name, Object value) {
            this.columns.add(ConditionColumn.of(name, value, MatchingType.NOT_EQUAL));
            return this;
        }

        public Builder in(String name, Collection<?> value) {
            this.columns.add(ConditionColumn.of(name, value, MatchingType.IN));
            return this;
        }

        public Builder in(String name, Object... value) {
            this.columns.add(ConditionColumn.of(name, value, MatchingType.IN));
            return this;
        }

        public Builder isNull(String name) {
            this.columns.add(ConditionColumn.of(name, null, MatchingType.NULL));
            return this;
        }

        public Builder notNull(String name) {
            this.columns.add(ConditionColumn.of(name, null, MatchingType.NOT_NULL));
            return this;
        }

        public Builder startsWith(String name, String value) {
            this.columns.add(ConditionColumn.of(name, value, MatchingType.STARTING));
            return this;
        }

        public Builder endsWith(String name, String value) {
            this.columns.add(ConditionColumn.of(name, value, MatchingType.ENDING));
            return this;
        }

        public Builder contains(String name, String value) {
            this.columns.add(ConditionColumn.of(name, value, MatchingType.CONTAINING));
            return this;
        }

        public <T extends Comparable<?>> Builder gt(String name, T value) {
            this.columns.add(ConditionColumn.of(name, value, MatchingType.GT));
            return this;
        }

        public <T extends Comparable<?>> Builder ge(String name, T value) {
            this.columns.add(ConditionColumn.of(name, value, MatchingType.GE));
            return this;
        }

        public <T extends Comparable<?>> Builder lt(String name, T value) {
            this.columns.add(ConditionColumn.of(name, value, MatchingType.LT));
            return this;
        }

        public <T extends Comparable<?>> Builder le(String name, T value) {
            this.columns.add(ConditionColumn.of(name, value, MatchingType.LE));
            return this;
        }

        public List<ConditionColumn> build() {
            return columns;
        }
    }

}
