package io.github.honhimw.ddd.jpa.expression;

import io.github.honhimw.ddd.jpa.util.TypeDefs;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.type.internal.BasicTypeImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hon_him
 * @since 2025-03-10
 */

public class FragmentBuilder {

    private final List<SqmExpression<?>> expressions;

    private final NodeBuilder criteriaBuilder;

    private FragmentBuilder(NodeBuilder criteriaBuilder) {
        this.expressions = new ArrayList<>();
        this.criteriaBuilder = criteriaBuilder;
    }

    public static FragmentBuilder of(CriteriaBuilder criteriaBuilder) {
        if (criteriaBuilder instanceof NodeBuilder nodeBuilder) {
            return new FragmentBuilder(nodeBuilder);
        } else {
            throw new IllegalArgumentException("CriteriaBuilder must be instance of NodeBuilder");
        }
    }

    public <T> FragmentExpression<T> fragment(String raw) {
        return new FragmentExpression<>(raw, criteriaBuilder);
    }

    public <T> FragmentExpression<T> fragment(String raw, BasicTypeImpl<T> type) {
        return new FragmentExpression<>(raw, criteriaBuilder, type);
    }

    public FragmentExpression<String> str(String raw) {
        return new FragmentExpression<>(raw, criteriaBuilder, TypeDefs.basic().varchar());
    }

    public FragmentExpression<Integer> i32(String raw) {
        return new FragmentExpression<>(raw, criteriaBuilder, TypeDefs.basic().i32());
    }

    public FragmentExpression<Long> i64(String raw) {
        return new FragmentExpression<>(raw, criteriaBuilder, TypeDefs.basic().i64());
    }

    public FragmentExpression<Boolean> bool(String raw) {
        return new FragmentExpression<>(raw, criteriaBuilder, TypeDefs.basic().bool());
    }

    public FragmentExpression<LocalDateTime> localDateTime(String raw) {
        return new FragmentExpression<>(raw, criteriaBuilder, TypeDefs.basic().localDateTime());
    }

    public FragmentExpression<Instant> instant(String raw) {
        return new FragmentExpression<>(raw, criteriaBuilder, TypeDefs.basic().instant());
    }

    public FragmentBuilder append(String raw) {
        expressions.add(fragment(raw));
        return this;
    }

    public FragmentBuilder append(Expression<?> expression) {
        expressions.add((SqmExpression<?>) expression);
        return this;
    }

    public FragmentBuilder append(int i) {
        return append(str(String.valueOf(i)));
    }

    public FragmentBuilder append(long l) {
        return append(str(String.valueOf(l)));
    }

    public FragmentBuilder append(float f) {
        return append(str(String.valueOf(f)));
    }

    public FragmentBuilder append(double d) {
        return append(str(String.valueOf(d)));
    }

    public FragmentBuilder append(boolean b) {
        return append(str(String.valueOf(b)));
    }

    public FragmentBuilder append(char c) {
        return append(str(String.valueOf(c)));
    }

    public FragmentBuilder append(Object s) {
        return append(str(String.valueOf(s)));
    }

    public Expression<?> build() {
        if (expressions.size() == 1) {
            return expressions.get(0);
        } else {
            return new MultiExpression<>(expressions, criteriaBuilder);
        }
    }

    public <T> Expression<T> build(BasicTypeImpl<T> type) {
        return new MultiExpression<>(expressions, criteriaBuilder, type);
    }

}
