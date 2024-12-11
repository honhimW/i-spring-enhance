package io.github.honhimw.ddd.jpa.util;

import io.github.honhimw.core.ConditionColumn;
import io.github.honhimw.core.MatchingType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-04-22
 */

public class JsonWhereClause {

    public static <T> Specification<T> spec(String fieldName, List<ConditionColumn> attrs) {
        Assert.state(StringUtils.isNotBlank(fieldName), "fieldName can not be blank");
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            attrs.forEach(conditionColumn -> {
                String name = conditionColumn.getName();
                Object value = conditionColumn.getValue();
                MatchingType type = conditionColumn.getType();
                String key = StringUtils.removeStart(name, fieldName + ".");
                Assert.state(StringUtils.isNotBlank(key), "key can not be blank");
                String[] split = key.split("\\.");
                Expression<?>[] expressions = new Expression[split.length + 1];
                expressions[0] = root.get(fieldName);
                for (int i = 0; i < split.length; i++) {
                    expressions[i + 1] = cb.literal(split[i]);
                }
                Expression<String> jsonExtractPathText = cb.function("json_extract_path_text", String.class, expressions);
                Predicate predicate = null;
                switch (type) {
                    case EQUAL -> predicate = cb.equal(jsonExtractPathText, value);
                    case NOT_EQUAL -> predicate = cb.notEqual(jsonExtractPathText, value);
                    case NULL -> predicate = cb.isNull(jsonExtractPathText);
                    case NOT_NULL -> predicate = cb.isNotNull(jsonExtractPathText);
                    case IN -> {
                        CriteriaBuilder.In<String> in = cb.in(jsonExtractPathText);
                        if (value instanceof Collection<?> collection) {
                            collection.stream().map(String::valueOf).forEach(in::value);
                            predicate = in;
                        } else if (value.getClass().isArray()) {
                            Object[] _array = (Object[]) value;
                            for (Object o : _array) {
                                String _str = String.valueOf(o);
                                in.value(_str);
                            }
                            predicate = in;
                        }
                    }
                    case STARTING -> predicate = cb.like(jsonExtractPathText, value + "%");
                    case CONTAINING -> predicate = cb.like(jsonExtractPathText, "%" + value + "%");
                    case ENDING -> predicate = cb.like(jsonExtractPathText, "%" + value);
                }
                if (Objects.nonNull(predicate)) {
                    predicates.add(predicate);
                }
            });
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

}
