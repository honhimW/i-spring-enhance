package io.github.honhimw.ddd.jimmer.convert;

import io.github.honhimw.core.ConditionColumn;
import io.github.honhimw.core.IPageRequest;
import io.github.honhimw.core.MatchingType;
import io.github.honhimw.ddd.jimmer.domain.Specification;
import io.github.honhimw.ddd.jimmer.util.IFetcher;
import io.github.honhimw.ddd.jimmer.util.IProps;
import io.github.honhimw.util.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.TargetLevel;
import org.babyfish.jimmer.sql.ast.LikeMode;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.PropExpression;
import org.babyfish.jimmer.sql.ast.impl.Expr;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Example;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author honhimW
 * @since 2025-05-28
 */

public class IExampleSpecification implements Specification.Query {

    private final IPageRequest<?> iPageRequest;

    @SuppressWarnings("unused")
    public IExampleSpecification(ConditionColumn single) {
        this(IPageRequest.of(single));
    }

    @SuppressWarnings("unused")
    public IExampleSpecification(ConditionColumn... multiple) {
        this(IPageRequest.of(multiple));
    }

    public IExampleSpecification(IPageRequest<?> iPageRequest) {
        this.iPageRequest = iPageRequest;
    }

    @Override
    public Predicate toPredicate(IProps root, MutableRootQuery<?> query, IFetcher<?> fetcher) {
        Assert.notNull(root, "Root must not be null!");
        Assert.notNull(query, "RootQuery must not be null!");
        Assert.notNull(iPageRequest, "iPageRequest must not be null!");

        Object condition = iPageRequest.getCondition();
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(condition)) {
            Predicate predicate = QueryByExamplePredicateBuilder.getPredicate(root, Example.of(condition));
            if (Objects.nonNull(predicate)) {
                predicates.add(predicate);
            }
        }
        List<ConditionColumn> conditions = iPageRequest.getConditions();
        if (CollectionUtils.isNotEmpty(conditions)) {
            predicates.add(getPredicate(conditions, root));
        }

        if (predicates.isEmpty()) {
            return Expr._true();
        }

        if (predicates.size() == 1) {
            return predicates.iterator().next();
        }

        return Expr.and(predicates);
    }

    private Predicate getPredicate(List<ConditionColumn> conditionColumns, IProps root) {
        Boolean matchAll = iPageRequest.getMatchAll();

        Map<String, List<ConditionColumn>> groups = conditionColumns.stream().collect(Collectors.groupingBy(ConditionColumn::getGroup));
        if (groups.size() == 1) {
            // Only one group
            Map<String, List<ConditionColumn>> keyConditions = conditionColumns.stream()
                .collect(Collectors.groupingBy(ConditionColumn::getName));
            List<Predicate> predicates = getPredicates("", keyConditions, root, new PathNode("root", null, ""));
            return matchAll ? Expr.and(predicates) : Expr.or(predicates);
        } else {
            List<Predicate> finalPredicates = new ArrayList<>(groups.size());
            List<List<Predicate>> roots = groups.values().stream()
                .map(ccs -> {
                    Map<String, List<ConditionColumn>> kcs = ccs.stream().collect(Collectors.groupingBy(ConditionColumn::getName));
                    return getPredicates("", kcs, root, new PathNode("root", null, ""));
                })
                .toList();
            if (matchAll) {
                // Multiple groups and all match
                roots.stream()
                    .map(Expr::or)
                    .forEach(finalPredicates::add);
                return Expr.and(finalPredicates);
            } else {
                roots.stream()
                    .map(Expr::and)
                    .forEach(finalPredicates::add);
                return Expr.or(finalPredicates);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Predicate> getPredicates(String path, Map<String, List<ConditionColumn>> groups, IProps root, PathNode currentNode) {
        List<Predicate> predicates = new ArrayList<>();
        Predicate predicate = null;

        Set<String> paths = groups.keySet();

        Map<String, ImmutableProp> props = root.props();
        for (Map.Entry<String, ImmutableProp> entry : props.entrySet()) {
            String name = entry.getKey();
            String currentPath = StringUtils.isBlank(path) ? name : path + "." + name;
            if (paths.stream().noneMatch(p -> p.startsWith(currentPath))) {
                continue;
            }
            ImmutableProp immutableProp = entry.getValue();
            if (immutableProp.isReference(TargetLevel.ENTITY)) {
                Object attributeValue = Optional.ofNullable(groups.get(currentPath))
                    .map(conditionColumns -> conditionColumns.get(0))
                    .map(ConditionColumn::getValue)
                    .orElse(null);
                PathNode node = currentNode.add(name, attributeValue);
                if (node.spansCycle()) {
                    throw new InvalidDataAccessApiUsageException(
                        String.format("Path '%s' must not span a cyclic property reference!%n%s", currentPath, node));
                }
                IProps join = root.join(name);
                predicates.addAll(getPredicates(currentPath, groups, join, node));
                continue;
            }

            if (immutableProp.isReference(TargetLevel.OBJECT)) {
                Object attributeValue = Optional.ofNullable(groups.get(currentPath))
                    .map(conditionColumns -> conditionColumns.get(0))
                    .map(ConditionColumn::getValue)
                    .orElse(null);
                PathNode node = currentNode.add(name, attributeValue);
                if (node.spansCycle()) {
                    throw new InvalidDataAccessApiUsageException(
                        String.format("Path '%s' must not span a cyclic property reference!%n%s", currentPath, node));
                }
                predicates.addAll(getPredicates(currentPath, groups, root.embed(name), node));
                continue;
            }

            if (groups.containsKey(currentPath)) {
                List<ConditionColumn> group = groups.get(currentPath);
                Class<?> returnClass = handlePrimitive(immutableProp.getReturnClass());
                for (ConditionColumn cc : group) {
                    Object conditionValue = cc.getValue();
                    // Condition value is not null
                    if (Objects.nonNull(conditionValue)) {
                        Class<?> valueType = conditionValue.getClass();
                        // If the matching mode is `IN` type, the value must be an array or collection type
                        if (cc.getType() == MatchingType.IN) {
                            PropExpression<Object> expression = root.get(name);
                            Collection collection;
                            if (conditionValue.getClass().isArray()) {
                                Object[] value = (Object[]) conditionValue;
                                collection = Arrays.stream(value).toList();
                            } else if (conditionValue instanceof Collection) {
                                collection = (Collection) conditionValue;
                            } else {
                                continue;
                            }
                            collection = collection.stream()
                                .map(o -> readValue(returnClass, o))
                                .filter(Objects::nonNull)
                                .toList();
                            Assert.state(CollectionUtils.isNotEmpty(collection), "IN collection must not be empty");
                            predicate = expression.in(collection);
                        } else {
                            conditionValue = readValue(returnClass, conditionValue);
                            // String: starts with, contains, ends with
                            if (String.class.isAssignableFrom(returnClass)) {
                                PropExpression.Str expression = root.str(name);
                                String stringValue = String.valueOf(conditionValue);
                                switch (cc.getType()) {
                                    case CONTAINING -> predicate = expression.like(stringValue, LikeMode.ANYWHERE);
                                    case STARTING -> predicate = expression.like(stringValue, LikeMode.START);
                                    case ENDING -> predicate = expression.like(stringValue, LikeMode.END);
                                    default -> predicate = null;
                                }
                            } else if (Number.class.isAssignableFrom(returnClass)) {
                                PropExpression.Num expression = root.num(name);
                                Number value = (Number) conditionValue;
                                switch (cc.getType()) {
                                    case GT -> predicate = expression.gt(value);
                                    case GE -> predicate = expression.ge(value);
                                    case LT -> predicate = expression.lt(value);
                                    case LE -> predicate = expression.le(value);
                                    default -> predicate = null;
                                }
                            } else if (Comparable.class.isAssignableFrom(returnClass) && Comparable.class.isAssignableFrom(valueType)) {
                                PropExpression.Cmp<Comparable<?>> expression = root.cmp(name);
                                Comparable<?> value = (Comparable<?>) conditionValue;
                                switch (cc.getType()) {
                                    case GT -> predicate = expression.gt(value);
                                    case GE -> predicate = expression.ge(value);
                                    case LT -> predicate = expression.lt(value);
                                    case LE -> predicate = expression.le(value);
                                    default -> predicate = null;
                                }
                            } else if (Boolean.class.isAssignableFrom(returnClass) && Boolean.class.isAssignableFrom(valueType)) {
                                PropExpression<Boolean> expression = root.get(name);
                                Boolean value = (Boolean) conditionValue;
                                switch (cc.getType()) {
                                    case NOT_EQUAL ->
                                        predicate = Boolean.TRUE.equals(value) ? Expr.isFalse(expression) : Expr.isTrue(expression);
                                    case EQUAL ->
                                        predicate = Boolean.TRUE.equals(value) ? Expr.isTrue(expression) : Expr.isFalse(expression);
                                    default -> predicate = null;
                                }
                            }
                        }
                    }
                    // If the condition does not hit any of the corresponding predicate
                    if (Objects.isNull(predicate)) {
                        PropExpression<Object> expression = root.get(name);
                        // Ignore non-simple objects
                        if (conditionValue instanceof Map) {
                            continue;
                        }
                        // Any Type: equal, not equal, null, not null
                        switch (cc.getType()) {
                            case NOT_EQUAL -> {
                                if (Objects.isNull(conditionValue)) {
                                    continue;
                                }
                                predicate = expression.ne(conditionValue);
                            }
                            case EQUAL -> {
                                if (Objects.isNull(conditionValue)) {
                                    continue;
                                }
                                predicate = expression.eq(conditionValue);
                            }
                            case NULL -> predicate = expression.isNull();
                            case NOT_NULL -> predicate = expression.isNotNull();
                            default -> {
                            }
                        }
                    }
                    if (Objects.nonNull(predicate)) {
                        predicates.add(predicate);
                    }
                }
            }

        }
        return predicates;
    }

    @SuppressWarnings("unchecked")
    private static <T> T readValue(Class<T> type, Object value) {
        if (type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return JsonUtils.mapper().convertValue(value, type);
    }

    private static Class<?> handlePrimitive(Class<?> primitive) {
        if (primitive.isPrimitive()) {
            if (primitive == boolean.class) {
                return Boolean.class;
            } else if (primitive == int.class) {
                return Integer.class;
            } else if (primitive == byte.class) {
                return Byte.class;
            } else if (primitive == char.class) {
                return Character.class;
            } else if (primitive == short.class) {
                return Short.class;
            } else if (primitive == long.class) {
                return Long.class;
            } else if (primitive == float.class) {
                return Float.class;
            } else if (primitive == double.class) {
                return Double.class;
            }
        }
        return primitive;
    }

}
