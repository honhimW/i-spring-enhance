package io.github.honhimw.ddd.jimmer.acl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.honhimw.core.ConditionColumn;
import io.github.honhimw.core.MatchingType;
import io.github.honhimw.ddd.common.Ace;
import io.github.honhimw.ddd.common.ResourceMod;
import io.github.honhimw.ddd.common.SudoSupports;
import io.github.honhimw.ddd.jimmer.convert.IExampleSpecification;
import io.github.honhimw.ddd.jimmer.domain.Specification;
import io.github.honhimw.ddd.jimmer.util.Utils;
import io.github.honhimw.util.JsonUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

/**
 * @author honhimW
 * @since 2025-05-29
 */

@Slf4j
public abstract class AbstractAclExecutor<T> implements AclExecutor {

    private final JSqlClientImplementor sqlClient;

    private final TableProxy<T> tableProxy;

    private final String dataDomain;

    public final ResourceMod defaultMod;

    private final Map<String, TableProxy<?>> entityTypeMap = new ConcurrentHashMap<>();

    public AbstractAclExecutor(JSqlClientImplementor sqlClient, TableProxy<T> tableProxy, String dataDomain, ResourceMod defaultMod) {
        this.sqlClient = sqlClient;
        this.tableProxy = tableProxy;
        this.dataDomain = dataDomain;
        this.defaultMod = defaultMod;
    }

    protected abstract boolean guard();

    protected boolean isRoot() {
        return SudoSupports.isSudo();
    }

    @Nonnull
    protected abstract Map<String, Object> getAttributes();

    @Nonnull
    protected abstract List<? extends Ace> getAcl();

    @Override
    public @Nullable Specification.Query read() {
        if (!guard()) {
            return null;
        }

        Map<String, Object> attributes = getAttributes();
        List<? extends Ace> acl = getAcl();

        List<? extends Ace> currentAcl = acl.stream()
            .filter(aclDTO -> Strings.CS.equals(dataDomain, aclDTO.getDataDomain()))
            .toList();

        Map<String, List<Specification.Query>> groupedSpecifications = new HashMap<>();

        if (CollectionUtils.isNotEmpty(currentAcl)) {
            // If ACL is not empty, and all not allow read, the query condition is always false, that is, any permission can be read
            if (currentAcl.stream().noneMatch(aclDTO -> aclDTO.getMod().canRead())) {
                return Specification.Query._false();
            } else {
                // Otherwise, you can see the readable permission with parameters that meet the condition
                for (Ace ace : currentAcl) {
                    String parameterName = Objects.requireNonNullElse(ace.getName(), "");
                    String groupName = "__default__";
                    Matcher matcher = ACLUtils.GROUPED_PARAMETER_NAME.matcher(parameterName);
                    if (matcher.find()) {
                        groupName = matcher.group("group");
                        parameterName = matcher.group("paramName");
                    }
                    Specification.Query sSpecification = buildSpecification(ace, parameterName, attributes);
                    if (Objects.nonNull(sSpecification)) {
                        groupedSpecifications.compute(groupName, (s, specifications) -> {
                            if (Objects.isNull(specifications)) {
                                specifications = new ArrayList<>();
                            }
                            specifications.add(sSpecification);
                            return specifications;
                        });
                    }
                }
            }
        }
        AtomicReference<Specification.Query> _ref = new AtomicReference<>();
        if (MapUtils.isNotEmpty(groupedSpecifications)) {
            // Apply `or` inside groups, `and` between groups
            List<Specification.Query> list = groupedSpecifications.values().stream().map(Specification::anyOf).toList();
            _ref.set(Specification.allOf(list));
        } else {
            // Default mod when ACL is empty
            if (isRoot()) {
                _ref.set(Specification.Query._true());
            } else {
                _ref.set(defaultMod.canRead() ? Specification.Query._true() : Specification.Query._false());
            }
        }
        return _ref.get();
    }

    @Override
    public void write() {
        if (!guard()) {
            return;
        }
        List<? extends Ace> acl = getAcl();
        List<? extends Ace> currentACLs = acl.stream()
            .filter(aclDTO -> Strings.CS.equals(dataDomain, aclDTO.getDataDomain()))
            .toList();
        if (CollectionUtils.isNotEmpty(currentACLs)) {
            // TODO Not yet supported row-level data write control
            if (currentACLs.stream().noneMatch(aclDTO -> aclDTO.getMod().canWrite())) {
                throw new UnsupportedOperationException("Current user is not allowed to write");
            }
        } else {
            // If the current data domain ACL is empty, it means that the default global ACL is RWX
            if (!defaultMod.canWrite()) {
                throw new UnsupportedOperationException("Current user is not allowed to write");
            }
        }
    }

    @Override
    public @Nullable Specification.Delete delete() {
        // TODO using readability and writability for delete check instead
        return null;
    }

    @Nullable
    protected <S extends T> Specification.Query buildSpecification(Ace ace, String parameterName, Map<String, Object> attributes) {
        if (StringUtils.isBlank(parameterName)) {
            if (ace.getMod().canRead()) {
                return Specification.Query._true();
            } else {
                return Specification.Query._false();
            }
        }
        Specification.Query subSpecification;
        if (StringUtils.isBlank(ace.getValue())) {
            return null;
        }
        Matcher subQueryV2 = ACLUtils.SUB_QUERY_PATTERN_V2.matcher(ace.getValue());
        if (subQueryV2.find()) {
            String _dataDomain = subQueryV2.group("dataDomain");
            TableProxy<?> subTableProxy = ACLUtils.DATA_DOMAIN_TABLE_MAP.get(_dataDomain);
            if (Objects.isNull(subTableProxy)) {
                return null;
            }
            subSpecification = new AclSubQuerySpecification(
                sqlClient,
                subTableProxy,
                parameterName,
                subQueryV2.group("rtProperty"),
                subQueryV2.group("property"),
                subQueryV2.group("value"),
                ace.getMatchingType(),
                attributes
            );
        } else {
            Matcher subQuery = ACLUtils.SUB_QUERY_PATTERN.matcher(ace.getValue());
            if (subQuery.find()) {
                String className = subQuery.group("className");
                TableProxy<?> entityType = entityTypeMap.computeIfAbsent(className, key -> {
                    try {
                        Class<?> aClass = Class.forName(className);
                        return Utils.getTable(aClass);
                    } catch (Exception e) {
                        log.warn("could not find entityType of: {}", className);
                        return null;
                    }
                });
                if (Objects.isNull(entityType)) {
                    return null;
                }
                subSpecification = new AclSubQuerySpecification(
                    sqlClient,
                    tableProxy,
                    parameterName,
                    subQuery.group("rtProperty"),
                    subQuery.group("property"),
                    subQuery.group("value"),
                    ace.getMatchingType(),
                    attributes
                );
            } else {
                String parameterValue = ace.getValue();
                Object value = parameterValue;
                String attributeName = tryGetAttributeName(parameterValue);
                if (StringUtils.isNotBlank(attributeName)) {
                    Object attribute = attributes.get(attributeName);
                    if (Objects.nonNull(attribute)) {
                        value = attribute;
                    } else {
                        return null;
                    }
                }
                if (ace.getMatchingType() == MatchingType.IN
                    && Strings.CS.startsWith(parameterValue, "[")
                    && Strings.CS.endsWith(parameterValue, "]")) {
                    try {
                        value = JsonUtils.mapper().readerFor(List.class).readValue(parameterValue);
                    } catch (JsonProcessingException e) {
                        log.warn("ACL parameter value can't parse into Collection type. Raw value: {}", parameterValue);
                        throw new RuntimeException(e);
                    }
                }
                ConditionColumn condition = ConditionColumn.of(parameterName, value, ace.getMatchingType());
                subSpecification = new IExampleSpecification(condition);
            }
        }
        if (!ace.getMod().canRead()) {
            subSpecification = Specification.not(subSpecification);
        }
        return subSpecification;
    }

    @Nullable
    protected String tryGetAttributeName(String parameterValue) {
        if (StringUtils.isNotBlank(parameterValue)) {
            Matcher matcher = ACLUtils.CONTEXT_ATTRIBUTE_PATTERN.matcher(parameterValue);
            if (matcher.find()) {
                return matcher.group("attr");
            }
        }
        return null;
    }

}
