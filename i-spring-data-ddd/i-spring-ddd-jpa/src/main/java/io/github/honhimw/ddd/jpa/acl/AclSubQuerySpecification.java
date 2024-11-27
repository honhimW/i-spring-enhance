package io.github.honhimw.ddd.jpa.acl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.honhimw.core.ConditionColumn;
import io.github.honhimw.core.MatchingType;
import io.github.honhimw.ddd.jpa.util.IExampleSpecification;
import io.github.honhimw.util.JsonUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;


/**
 * @author hon_him
 * @since 2023-09-21
 */

@Slf4j
public class AclSubQuerySpecification<T, S extends T> implements Specification<S> {

    private final JpaEntityInformation<T, ?> hostEI;

    private final EntityType<?> subEntityType;

    private final String hostParamName;

    private final String rtProperty;

    private final String property;

    private final String value;

    private final MatchingType matchingType;

    private final Map<String, Object> attributes;

    public AclSubQuerySpecification(JpaEntityInformation<T, ?> hostEI, EntityType<?> subEntityType, String hostParamName, String rtProperty, String property, String value, MatchingType matchingType, Map<String, Object> attributes) {
        this.hostEI = hostEI;
        this.subEntityType = subEntityType;
        this.hostParamName = hostParamName;
        this.rtProperty = rtProperty;
        this.property = property;
        this.value = value;
        this.matchingType = matchingType;
        this.attributes = attributes;
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Predicate toPredicate(@Nonnull Root<S> root, @Nullable CriteriaQuery<?> query, @Nonnull CriteriaBuilder criteriaBuilder) {
        if (Objects.isNull(query)) {
            return null;
        }
        Path<Object> path = root.get(hostParamName);
        Subquery subquery = query.subquery(path.getJavaType());
        Root from = subquery.from(subEntityType);

        Path relativePath = from;
        String[] split = rtProperty.split("\\.");
        for (String s : split) {
            relativePath = relativePath.get(s);
        }
        Subquery select = subquery.select(relativePath);
        Object finalValue = value;
        String attributeName = tryGetAttributeName(value);
        if (StringUtils.isNotBlank(attributeName)) {
            Object attribute = attributes.get(attributeName);
            if (Objects.nonNull(attribute)) {
                finalValue = attribute;
            } else {
                return null;
            }
        }
        if (matchingType == MatchingType.IN
            && StringUtils.startsWith(value, "[")
            && StringUtils.endsWith(value, "]")) {
            try {
                finalValue = JsonUtils.mapper().readerFor(List.class).readValue(value);
            } catch (JsonProcessingException e) {
                log.warn("ACL parameter value can't parse into Collection type. Raw value: {}", value);
                throw new RuntimeException(e);
            }
        }
        if (matchingType == MatchingType.IN) {
            if (finalValue instanceof Collection<?> collection) {
                if (CollectionUtils.isEmpty(collection)) {
                    return criteriaBuilder.isTrue(criteriaBuilder.literal(false));
                }
            } else if (finalValue.getClass().isArray()) {
                if (ArrayUtils.getLength(finalValue) == 0) {
                    return criteriaBuilder.isTrue(criteriaBuilder.literal(false));
                }
            }
        }
        Predicate predicate = new IExampleSpecification<>(ConditionColumn.of(property, finalValue, matchingType)).toPredicate(from, query, criteriaBuilder);
        Subquery where = select.where(predicate);
        return root.get(hostEI.getIdAttribute()).in(where);
    }

    @Nullable
    private static String tryGetAttributeName(String parameterValue) {
        if (StringUtils.isNotBlank(parameterValue)) {
            Matcher matcher = ACLUtils.CONTEXT_ATTRIBUTE_PATTERN.matcher(parameterValue);
            if (matcher.find()) {
                return matcher.group("attr");
            }
        }
        return null;
    }
}
