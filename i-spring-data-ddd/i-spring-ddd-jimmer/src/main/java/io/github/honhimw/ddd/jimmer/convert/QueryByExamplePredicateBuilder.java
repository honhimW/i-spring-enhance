package io.github.honhimw.ddd.jimmer.convert;

import io.github.honhimw.ddd.jimmer.util.IProps;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.TargetLevel;
import org.babyfish.jimmer.sql.JoinType;
import org.babyfish.jimmer.sql.ast.LikeMode;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.StringExpression;
import org.babyfish.jimmer.sql.ast.impl.Expr;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.support.ExampleMatcherAccessor;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author honhimW
 * @since 2025-05-27
 */

public class QueryByExamplePredicateBuilder {

    @Nullable
    public static <T> Predicate getPredicate(IProps root, Example<T> example) {
        T probe = example.getProbe();
        ExampleMatcher matcher = example.getMatcher();
        Class<T> probeType = example.getProbeType();
        ExampleMatcher.MatchMode matchMode = matcher.getMatchMode();
        PathNode pathNode = new PathNode("root", null, example.getProbe());
        List<Predicate> predicate = getPredicates("", root, probe, probeType, matchMode, new ExampleMatcherAccessor(matcher), pathNode);
        return Expr.and(predicate);
    }

    static List<Predicate> getPredicates(String path, IProps from,
                                         Object value, Class<?> probeType,
                                         ExampleMatcher.MatchMode matchMode,
                                         ExampleMatcherAccessor exampleAccessor, PathNode currentNode) {
        Map<String, ImmutableProp> props = from.props();
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, ImmutableProp> entry : props.entrySet()) {
            String attribute = entry.getKey();
            String currentPath = !StringUtils.hasText(path) ? attribute : path + "." + attribute;
            if (exampleAccessor.isIgnoredPath(currentPath)) {
                continue;
            }
            Optional<Object> optionalValue = ImmutableObjects.isLoaded(value, attribute) ? Optional.ofNullable(ImmutableObjects.get(value, attribute)) : Optional.empty();

            if (optionalValue.isEmpty()) {
                if (exampleAccessor.getNullHandler().equals(ExampleMatcher.NullHandler.INCLUDE)) {
                    predicates.add(from.get(attribute).isNull());
                }
                continue;
            }

            Object attributeValue = optionalValue.get();

            if (attributeValue == Optional.empty()) {
                continue;
            }

            ImmutableProp immutableProp = entry.getValue();
            if (immutableProp.isReference(TargetLevel.ENTITY)) {
                PathNode node = currentNode.add(attribute, attributeValue);
                JoinType joinType = matchMode == ExampleMatcher.MatchMode.ALL ? JoinType.INNER : JoinType.LEFT;
                if (node.spansCycle()) {
                    throw new InvalidDataAccessApiUsageException(
                        String.format("Path '%s' from root %s must not span a cyclic property reference%n%s", currentPath,
                            ClassUtils.getShortName(probeType), node));
                }
                predicates.addAll(getPredicates(currentPath, from.join(attribute, joinType), attributeValue, probeType, matchMode, exampleAccessor, node));
                continue;
            }

            if (immutableProp.isReference(TargetLevel.OBJECT)) {
                predicates.addAll(getPredicates(currentPath, from.embed(attribute), attributeValue, probeType, matchMode, exampleAccessor, currentNode));
                continue;

            }
            if (String.class.isAssignableFrom(immutableProp.getReturnClass())) {
                StringExpression expression = from.str(attribute);
                String stringValue = String.valueOf(attributeValue);
                if (exampleAccessor.isIgnoreCaseForPath(currentPath)) {
                    expression = expression.lower();
                    stringValue = stringValue.toLowerCase();
                }

                switch (exampleAccessor.getStringMatcherForPath(currentPath)) {
                    case DEFAULT, EXACT -> predicates.add(expression.eq(stringValue));
                    case CONTAINING -> predicates.add(expression.like(stringValue, LikeMode.ANYWHERE));
                    case STARTING -> predicates.add(expression.like(stringValue, LikeMode.START));
                    case ENDING -> predicates.add(expression.like(stringValue, LikeMode.END));
                    default -> throw new IllegalArgumentException(
                        "Unsupported StringMatcher " + exampleAccessor.getStringMatcherForPath(currentPath));
                }

            } else {
                predicates.add(from.get(attribute).eq(attributeValue));
            }
        }
        return predicates;
    }

}
