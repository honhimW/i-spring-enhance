package io.github.honhimw.ddd.jpa.util;

import io.github.honhimw.core.ConditionColumn;
import io.github.honhimw.core.IPageRequest;
import io.github.honhimw.core.MatchingType;
import jakarta.annotation.Nonnull;
import jakarta.persistence.criteria.*;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.metamodel.*;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extend {@link Specification} based on {@link ConditionColumn}
 * </p>
 * {@link IPageRequest#getCondition()} using {@link QueryByExamplePredicateBuilder#getPredicate(Root, CriteriaBuilder,
 * Example)}
 * </p>
 * {@link IPageRequest#getConditions()}} transfer to {@link Predicate}
 *
 * @author hon_him
 * @since 2022-11-21
 */

public class IExampleSpecification<T> implements Specification<T> {

    private final IPageRequest<T> iPageRequest;

    private final EscapeCharacter escapeCharacter;

    @SuppressWarnings("unused")
    public IExampleSpecification(ConditionColumn single) {
        this(IPageRequest.of(single));
    }

    @SuppressWarnings("unused")
    public IExampleSpecification(ConditionColumn... multiple) {
        this(IPageRequest.of(multiple));
    }

    public IExampleSpecification(IPageRequest<T> iPageRequest) {
        this(iPageRequest, EscapeCharacter.DEFAULT);
    }

    public IExampleSpecification(IPageRequest<T> iPageRequest, EscapeCharacter escapeCharacter) {
        this.iPageRequest = iPageRequest;
        this.escapeCharacter = escapeCharacter;
    }

    @Override
    public Predicate toPredicate(@Nonnull Root<T> root, @Nullable CriteriaQuery<?> query, @Nonnull CriteriaBuilder cb) {
        Assert.notNull(root, "Root must not be null!");
        Assert.notNull(cb, "CriteriaBuilder must not be null!");
        Assert.notNull(iPageRequest, "iPageRequest must not be null!");

        T condition = iPageRequest.getCondition();
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.isNull(condition)) {
            try {
                condition = root.getModel().getJavaType().getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Root model type [" + root.getModel().getJavaType().getName() + "] must has a public no arg constructor.", e);
            }
        }
        // Spring-data-jpa, findByExample
        Predicate predicate = QueryByExamplePredicateBuilder.getPredicate(root, cb, Example.of(condition));
        if (Objects.nonNull(predicate)) {
            predicates.add(predicate);
        }
        List<ConditionColumn> conditions = iPageRequest.getConditions();
        if (CollectionUtils.isNotEmpty(conditions)) {
            predicates.add(getPredicate(conditions, cb, root, root.getModel()));
        }

        if (predicates.size() == 1) {
            return predicates.iterator().next();
        }

        return cb.and(predicates.toArray(Predicate[]::new));
    }

    private Predicate getPredicate(List<ConditionColumn> conditionColumns, CriteriaBuilder cb, Root<?> root,
                                   ManagedType<?> type) {
        Boolean matchAll = iPageRequest.getMatchAll();

        Map<String, List<ConditionColumn>> groups = conditionColumns.stream().collect(Collectors.groupingBy(ConditionColumn::getGroup));
        if (groups.size() == 1) {
            // Only one group
            Map<String, List<ConditionColumn>> keyConditions = conditionColumns.stream()
                .collect(Collectors.groupingBy(ConditionColumn::getName));
            List<Predicate> predicates = getPredicates("", keyConditions, cb, root, type, new PathNode("root", null, ""));
            return matchAll ? cb.and(predicates.toArray(Predicate[]::new)) : cb.or(predicates.toArray(Predicate[]::new));
        } else {
            List<Predicate> finalPredicates = new ArrayList<>(groups.size());
            List<List<Predicate>> roots = groups.values().stream()
                .map(ccs -> {
                    Map<String, List<ConditionColumn>> kcs = ccs.stream().collect(Collectors.groupingBy(ConditionColumn::getName));
                    return getPredicates("", kcs, cb, root, type, new PathNode("root", null, ""));
                })
                .toList();
            if (matchAll) {
                // Multiple groups and all match
                roots.stream()
                    .map(predicates -> cb.or(predicates.toArray(Predicate[]::new)))
                    .forEach(finalPredicates::add);
                return cb.and(finalPredicates.toArray(Predicate[]::new));
            } else {
                roots.stream()
                    .map(predicates -> cb.and(predicates.toArray(Predicate[]::new)))
                    .forEach(finalPredicates::add);
                return cb.or(finalPredicates.toArray(Predicate[]::new));
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Predicate> getPredicates(String path, Map<String, List<ConditionColumn>> groups, CriteriaBuilder cb,
                                          Path<?> root,
                                          ManagedType<?> type, PathNode currentNode) {
        List<Predicate> predicates = new ArrayList<>();
        Predicate predicate = null;

        Set<String> paths = groups.keySet();
        for (PluralAttribute<?, ?, ?> attribute : type.getPluralAttributes()) {
            String name = attribute.getName();
            String currentPath = StringUtils.isBlank(path) ? name : path + "." + name;
            if (paths.stream().noneMatch(p -> p.startsWith(currentPath))) {
                continue;
            }
            Type<?> elementType = attribute.getElementType();
            if (isAssociation(attribute)) {
                Object attributeValue = Optional.ofNullable(groups.get(currentPath))
                    .map(conditionColumns -> conditionColumns.get(0))
                    .map(ConditionColumn::getValue)
                    .orElse(null);
                PathNode node = currentNode.add(name, attributeValue);
                if (node.spansCycle()) {
                    throw new InvalidDataAccessApiUsageException(
                        String.format("Path '%s' must not span a cyclic property reference!%n%s", currentPath, node));
                }
                Join<Object, Object> join = ((From<?, ?>) root).join(name);
                predicates.addAll(getPredicates(currentPath, groups, cb, join, (ManagedType<?>) elementType, node));
            }
        }

        for (SingularAttribute<?, ?> attribute : type.getSingularAttributes()) {
            String name = attribute.getName();
            String currentPath = StringUtils.isBlank(path) ? name : path + "." + name;

            if (attribute.getPersistentAttributeType().equals(PersistentAttributeType.EMBEDDED)
                || (isAssociation(attribute) && !(root instanceof From))) {
                predicates
                    .addAll(getPredicates(currentPath, groups, cb, root.get(name), (ManagedType<?>) attribute.getType(),
                        currentNode));
                continue;
            }
            if (isAssociation(attribute)) {
                Object attributeValue = Optional.ofNullable(groups.get(currentPath))
                    .map(conditionColumns -> conditionColumns.get(0))
                    .map(ConditionColumn::getValue)
                    .orElse(null);
                PathNode node = currentNode.add(name, attributeValue);
                if (node.spansCycle()) {
                    throw new InvalidDataAccessApiUsageException(
                        String.format("Path '%s' must not span a cyclic property reference!%n%s", currentPath, node));
                }
                predicates.addAll(getPredicates(currentPath, groups, cb, ((From<?, ?>) root).join(name),
                    (ManagedType<?>) attribute.getType(), node));
                continue;
            }

            if (groups.containsKey(currentPath)) {
                List<ConditionColumn> group = groups.get(currentPath);
                Class<?> attributeJavaType = attribute.getJavaType();
                for (ConditionColumn cc : group) {
                    Object conditionValue = cc.getValue();
                    // Condition value is not null
                    if (Objects.nonNull(conditionValue)) {
                        Class<?> valueType = conditionValue.getClass();
                        // If the matching mode is `IN` type, the value must be an array or collection type
                        if (cc.getType() == MatchingType.IN) {
                            Expression<Object> expression = root.get(name);
                            Collection<?> collection;
                            if (conditionValue.getClass().isArray()) {
                                Object[] value = (Object[]) conditionValue;
                                collection = Arrays.stream(value).toList();
                            } else if (conditionValue instanceof Collection<?>) {
                                collection = (Collection<?>) conditionValue;
                            } else {
                                continue;
                            }
                            collection = collection.stream()
                                .map(o -> readValue(attributeJavaType, o))
                                .filter(Objects::nonNull)
                                .toList();
                            Assert.state(!collection.isEmpty(), "IN collection must not be empty");
                            In<Object> in = cb.in(expression);
                            for (Object o : collection) {
                                in = in.value(o);
                            }
                            predicate = in;
                        } else {
                            conditionValue = readValue(attributeJavaType, conditionValue);
                            // String: starts with, contains, ends with
                            if (attributeJavaType.equals(String.class)) {
                                Expression<String> expression = root.get(name);
                                switch (cc.getType()) {
                                    case CONTAINING -> predicate = cb.like(
                                        expression, //
                                        "%" + escapeCharacter.escape((String) conditionValue) + "%", //
                                        escapeCharacter.getEscapeCharacter() //
                                    );
                                    case STARTING -> predicate = cb.like(//
                                        expression, //
                                        escapeCharacter.escape((String) conditionValue) + "%", //
                                        escapeCharacter.getEscapeCharacter()); //
                                    case ENDING -> predicate = cb.like( //
                                        expression, //
                                        "%" + escapeCharacter.escape((String) conditionValue), //
                                        escapeCharacter.getEscapeCharacter()); //
                                    default -> predicate = null;
                                }
                                // Number: greater than, greater than or equal to, less than, less than or equal to
                            } else if (Number.class.isAssignableFrom(attributeJavaType)) {
                                Expression<Number> expression = root.get(name);
                                switch (cc.getType()) {
                                    case GT -> predicate = cb.gt(expression, (Number) conditionValue);
                                    case GE -> predicate = cb.ge(expression, (Number) conditionValue);
                                    case LT -> predicate = cb.lt(expression, (Number) conditionValue);
                                    case LE -> predicate = cb.le(expression, (Number) conditionValue);
                                    default -> predicate = null;
                                }
                                // Comparable: greater than, greater than or equal to, less than, less than or equal to
                            } else if (Comparable.class.isAssignableFrom(attributeJavaType)) {
                                Expression<Comparable> expression = root.get(name);
                                switch (cc.getType()) {
                                    case GT -> predicate = cb.greaterThan(expression, (Comparable) conditionValue);
                                    case GE ->
                                        predicate = cb.greaterThanOrEqualTo(expression, (Comparable) conditionValue);
                                    case LT -> predicate = cb.lessThan(expression, (Comparable) conditionValue);
                                    case LE ->
                                        predicate = cb.lessThanOrEqualTo(expression, (Comparable) conditionValue);
                                    default -> predicate = null;
                                }
                                // Boolean: equal, not equal
                            } else if (Boolean.class.isAssignableFrom(attributeJavaType)
                                       && Boolean.class.isAssignableFrom(valueType)) {
                                Expression<Boolean> expression = root.get(name);
                                Boolean value = (Boolean) conditionValue;
                                switch (cc.getType()) {
                                    case NOT_EQUAL ->
                                        predicate = Boolean.TRUE.equals(value) ? cb.isFalse(expression) : cb.isTrue(expression);
                                    case EQUAL ->
                                        predicate = Boolean.TRUE.equals(value) ? cb.isTrue(expression) : cb.isFalse(expression);
                                    default -> predicate = null;
                                }
                            }
                        }
                    }
                    // If the condition does not hit any of the corresponding predicate
                    if (Objects.isNull(predicate)) {
                        Expression<Object> expression = root.get(name);
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
                                predicate = cb.notEqual(expression, conditionValue);
                            }
                            case EQUAL -> {
                                if (Objects.isNull(conditionValue)) {
                                    continue;
                                }
                                predicate = cb.equal(expression, conditionValue);
                            }
                            case NULL -> predicate = cb.isNull(expression);
                            case NOT_NULL -> predicate = cb.isNotNull(expression);
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
    public static <T> T readValue(Class<T> type, Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return JacksonUtils.readValue(type, value);
    }

    private static final Set<PersistentAttributeType> ASSOCIATION_TYPES;

    static {
        ASSOCIATION_TYPES = EnumSet.of(PersistentAttributeType.MANY_TO_MANY, //
            PersistentAttributeType.MANY_TO_ONE, //
            PersistentAttributeType.ONE_TO_MANY, //
            PersistentAttributeType.ONE_TO_ONE);
    }

    private static boolean isAssociation(Attribute<?, ?> attribute) {
        return ASSOCIATION_TYPES.contains(attribute.getPersistentAttributeType());
    }

    @SuppressWarnings("all")
    private static class PathNode {

        String name;
        @Nullable
        PathNode parent;
        List<PathNode> siblings = new ArrayList<>();
        @Nullable
        Object value;

        PathNode(String edge, @Nullable PathNode parent, @Nullable Object value) {

            this.name = edge;
            this.parent = parent;
            this.value = value;
        }

        PathNode add(String attribute, @Nullable Object value) {

            PathNode node = new PathNode(attribute, this, value);
            siblings.add(node);
            return node;
        }

        boolean spansCycle() {

            if (value == null) {
                return false;
            }

            String identityHex = ObjectUtils.getIdentityHexString(value);
            PathNode current = parent;

            while (current != null) {

                if (current.value != null && ObjectUtils.getIdentityHexString(current.value).equals(identityHex)) {
                    return true;
                }
                current = current.parent;
            }

            return false;
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            if (parent != null) {
                sb.append(parent);
                sb.append(" -");
                sb.append(name);
                sb.append("-> ");
            }

            sb.append("[{ ");
            sb.append(ObjectUtils.nullSafeToString(value));
            sb.append(" }]");
            return sb.toString();
        }
    }

}
