package io.github.honhimw.ddd.jimmer.acl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.honhimw.core.ConditionColumn;
import io.github.honhimw.core.MatchingType;
import io.github.honhimw.ddd.jimmer.convert.IExampleSpecification;
import io.github.honhimw.ddd.jimmer.domain.Specification;
import io.github.honhimw.ddd.jimmer.util.IFetcher;
import io.github.honhimw.ddd.jimmer.util.IProps;
import io.github.honhimw.util.JsonUtils;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.PropExpression;
import org.babyfish.jimmer.sql.ast.impl.Expr;
import org.babyfish.jimmer.sql.ast.query.ConfigurableSubQuery;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * @author honhimW
 * @since 2025-05-29
 */

@Slf4j
public class AclSubQuerySpecification implements Specification.Query {

    private final JSqlClientImplementor sqlClient;

    private final TableProxy<?> subTableProxy;

    private final String hostParamName;

    private final String rtProperty;

    private final String property;

    private final String value;

    private final MatchingType matchingType;

    private final Map<String, Object> attributes;

    public AclSubQuerySpecification(JSqlClientImplementor sqlClient, TableProxy<?> subTableProxy, String hostParamName, String rtProperty, String property, String value, MatchingType matchingType, Map<String, Object> attributes) {
        this.sqlClient = sqlClient;
        this.subTableProxy = subTableProxy;
        this.hostParamName = hostParamName;
        this.rtProperty = rtProperty;
        this.property = property;
        this.value = value;
        this.matchingType = matchingType;
        this.attributes = attributes;
    }

    @Override
    public Predicate toPredicate(IProps root, MutableRootQuery<?> query, IFetcher<?> fetcher) {
        if (Objects.isNull(query)) {
            return null;
        }
        IProps subProps = IProps.of(subTableProxy);
        PropExpression<Object> rtExpression = IProps.get(subProps, rtProperty);

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
                    return Expr._false();
                }
            } else if (finalValue.getClass().isArray()) {
                if (ArrayUtils.getLength(finalValue) == 0) {
                    return Expr._false();
                }
            }
        }

        Predicate predicate = new IExampleSpecification(ConditionColumn.of(property, finalValue, matchingType)).toPredicate(subProps, null, null);

        ConfigurableSubQuery<Object> subQuery = this.sqlClient.createSubQuery(subTableProxy)
            .where(predicate)
            .select(rtExpression);

        PropExpression<Object> expression = root.get(hostParamName);
        return expression.in(subQuery);
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
