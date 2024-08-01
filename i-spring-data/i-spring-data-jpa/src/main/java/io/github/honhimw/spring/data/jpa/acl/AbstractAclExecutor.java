package io.github.honhimw.spring.data.jpa.acl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.honhimw.spring.data.common.Ace;
import io.github.honhimw.spring.data.common.AclDataDomain;
import io.github.honhimw.spring.data.common.ResourceMod;
import io.github.honhimw.spring.data.jpa.util.IExampleSpecification;
import io.github.honhimw.spring.data.jpa.util.Specifications;
import io.github.honhimw.spring.model.IPageRequest;
import io.github.honhimw.spring.util.JsonUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

/**
 * @author hon_him
 * @since 2023-12-28
 */

@Slf4j
public abstract class AbstractAclExecutor<T> implements AclExecutor<T> {

    public final ResourceMod defaultMod;

    private final JpaEntityInformation<T, ?> ei;

    private final EntityManager em;

    private final String dataDomain;

    private final Map<String, EntityType<?>> entityTypeMap = new ConcurrentHashMap<>();

    public AbstractAclExecutor(@Nonnull ResourceMod defaultMod, @Nonnull JpaEntityInformation<T, ?> ei, @Nonnull EntityManager em, @Nonnull String dataDomain) {
        this.defaultMod = defaultMod;
        this.ei = ei;
        this.em = em;
        this.dataDomain = dataDomain;

        if (ACLUtils.DATA_DOMAIN_ENTITY_MAP.isEmpty()) {
            for (EntityType<?> entity : em.getMetamodel().getEntities()) {
                Class<?> javaType = entity.getJavaType();
                if (javaType.isAnnotationPresent(AclDataDomain.class)) {
                    AclDataDomain annotation = javaType.getAnnotation(AclDataDomain.class);
                    String _dataDomain = annotation.value();
                    ACLUtils.DATA_DOMAIN_ENTITY_MAP.put(_dataDomain, entity);
                }
            }
        }
    }

    protected abstract boolean guard();

    protected abstract boolean isRoot();

    @Nonnull
    protected abstract Map<String, Object> getAttributes();

    @Nonnull
    protected abstract List<? extends Ace> getAcl();

    @Nullable
    @Override
    public <S extends T> Specification<S> read() {
        if (!guard()) {
            return null;
        }

        Map<String, Object> attributes = getAttributes();
        List<? extends Ace> acl = getAcl();

        List<? extends Ace> currentAcl = acl.stream()
            .filter(aclDTO -> StringUtils.equals(dataDomain, aclDTO.getDataDomain()))
            .toList();

        Map<String, List<Specification<S>>> groupedSpecifications = new HashMap<>();

        if (CollectionUtils.isNotEmpty(currentAcl)) {
            // ACL不为空, 且全部不允许读时查询条件永远为false, 也就是任意可读权限均可读
            if (currentAcl.stream().noneMatch(aclDTO -> aclDTO.getMod().canRead())) {
                return (root, query, cb) -> cb.isTrue(cb.literal(false));
            } else {
                // 否则可以查看可读权限的带参数条件满足条件下的数据
                for (Ace ace : currentAcl) {
                    String parameterName = Objects.requireNonNullElse(ace.getName(), "");
                    String groupName = "__default__";
                    Matcher matcher = ACLUtils.GROUPED_PARAMETER_NAME.matcher(parameterName);
                    if (matcher.find()) {
                        groupName = matcher.group("group");
                        parameterName = matcher.group("paramName");
                    }
                    Specification<S> sSpecification = buildSpecification(ace, parameterName, attributes);
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
        AtomicReference<Specification<S>> _ref = new AtomicReference<>();
        if (MapUtils.isNotEmpty(groupedSpecifications)) {
            // 组内or, 组间and
            List<Specification<S>> list = groupedSpecifications.values().stream().map(Specification::anyOf).toList();
            _ref.set(Specification.allOf(list));
        } else {
            // 控制项为空的时候按照默认mod条件
            if (isRoot()) {
                _ref.set(Specifications.isTrue());
            } else {
                _ref.set(defaultMod.canRead() ? Specifications.isTrue() : Specifications.isFalse());
            }
        }
        return _ref.get();
    }

    @Override
    public void write() throws UnsupportedOperationException {
        if (!guard()) {
            return;
        }
        List<? extends Ace> acl = getAcl();
        List<? extends Ace> currentACLs = acl.stream()
            .filter(aclDTO -> StringUtils.equals(dataDomain, aclDTO.getDataDomain()))
            .toList();
        if (CollectionUtils.isNotEmpty(currentACLs)) {
            // TODO 暂不支持行级数据写入控制
            if (currentACLs.stream().noneMatch(aclDTO -> aclDTO.getMod().canWrite())) {
                throw new UnsupportedOperationException("当前用户不允许写入");
            }
        } else {
            // 当前数据域ACL为空, 表示默认全局的RWX权限
            if (!defaultMod.canWrite()) {
                throw new UnsupportedOperationException("当前用户不允许写入");
            }
        }
    }


    @Nullable
    protected <S extends T> Specification<S> buildSpecification(Ace ace, String parameterName, Map<String, Object> attributes) {
        if (StringUtils.isBlank(parameterName)) {
            if (ace.getMod().canRead()) {
                return Specifications.isTrue();
            } else {
                return Specifications.isFalse();
            }
        }
        Specification<S> tSpecification;
        if (StringUtils.isBlank(ace.getValue())) {
            return null;
        }
        Matcher subQueryV2 = ACLUtils.SUB_QUERY_PATTERN_V2.matcher(ace.getValue());
        if (subQueryV2.find()) {
            String _dataDomain = subQueryV2.group("dataDomain");
            EntityType<?> entityType = ACLUtils.DATA_DOMAIN_ENTITY_MAP.get(_dataDomain);
            if (Objects.isNull(entityType)) {
                return null;
            }
            tSpecification = new AclSubQuerySpecification<>(
                ei,
                entityType,
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
                EntityType<?> entityType = entityTypeMap.computeIfAbsent(className, key -> {
                    try {
                        Class<?> aClass = Class.forName(className);
                        return em.getMetamodel().entity(aClass);
                    } catch (Exception e) {
                        log.warn("could not find entityType of: {}", className);
                        return null;
                    }
                });
                if (Objects.isNull(entityType)) {
                    return null;
                }
                tSpecification = new AclSubQuerySpecification<>(
                    ei,
                    entityType,
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
                if (ace.getMatchingType() == IPageRequest.MatchingType.IN
                    && StringUtils.startsWith(parameterValue, "[")
                    && StringUtils.endsWith(parameterValue, "]")) {
                    try {
                        value = JsonUtils.getObjectMapper().readerFor(List.class).readValue(parameterValue);
                    } catch (JsonProcessingException e) {
                        log.warn("ACL parameter value can't parse into Collection type. Raw value: {}", parameterValue);
                        throw new RuntimeException(e);
                    }
                }
                IPageRequest.ConditionColumn condition = IPageRequest.ConditionColumn.of(parameterName, value, ace.getMatchingType());
                tSpecification = new IExampleSpecification<>(condition);
            }
        }
        if (!ace.getMod().canRead()) {
            tSpecification = Specification.not(tSpecification);
        }
        return tSpecification;
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
