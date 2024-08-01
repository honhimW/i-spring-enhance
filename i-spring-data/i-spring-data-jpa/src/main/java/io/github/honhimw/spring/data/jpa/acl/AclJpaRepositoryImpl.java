package io.github.honhimw.spring.data.jpa.acl;

import io.github.honhimw.spring.data.common.AclDataDomain;
import io.github.honhimw.spring.data.common.ResourceMod;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author hon_him
 * @since 2023-04-21
 */

public class AclJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> {

    private final JpaEntityInformation<T, ?> ei;

    private final EntityManager em;

    private final AclExecutor<T> aclExecutor;
    private final String dataDomain;
    private final boolean read;
    private final boolean write;
    private final boolean execute;

    public AclJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager, AclProvider aclProvider) {
        super(entityInformation, entityManager);
        this.ei = entityInformation;
        this.em = entityManager;
        Class<T> javaType = getDomainClass();
        ResourceMod mod;
        if (javaType.isAnnotationPresent(AclDataDomain.class)) {
            AclDataDomain annotation = javaType.getAnnotation(AclDataDomain.class);
            this.dataDomain = annotation.value();
            this.read = annotation.read();
            this.write = annotation.write();
            this.execute = annotation.execute();
            mod = annotation.defaultMod();
        } else {
            this.dataDomain = null;
            this.read = false;
            this.write = false;
            this.execute = false;
            mod = ResourceMod.RWX;
        }
        this.aclExecutor = aclProvider.getExecutor(ei, em, dataDomain, mod);
    }

    /*
     * ===================================================================================================
     * Specification extension ACL start
     * ===================================================================================================
     */

    @Nonnull
    @Override
    protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, @Nonnull Class<S> domainClass, @Nonnull Sort sort) {
        if (read) {
            Specification<S> aclReadSpec = aclExecutor.read();
            if (Objects.nonNull(aclReadSpec)) {
                spec = aclReadSpec.and(spec);
            }
        }
        return super.getQuery(spec, domainClass, sort);
    }

    @Nonnull
    @Override
    protected <S extends T> TypedQuery<Long> getCountQuery(Specification<S> spec, @Nonnull Class<S> domainClass) {
        if (read) {
            Specification<S> aclReadSpec = aclExecutor.read();
            if (Objects.nonNull(aclReadSpec)) {
                spec = aclReadSpec.and(spec);
            }
        }
        return super.getCountQuery(spec, domainClass);
    }

    /*
     * ===================================================================================================
     * Specification extension ACL end
     * ===================================================================================================
     * Find start
     * ===================================================================================================
     */

    @Nonnull
    @Override
    public Optional<T> findById(@Nonnull ID id) {
        if (read) {
            Specification<T> spec = (root, query, criteriaBuilder) -> {
                Path<?> path = root.get(ei.getIdAttribute());
                Predicate idPredicate = criteriaBuilder.equal(path, id);

                return criteriaBuilder.and(idPredicate);
            };
            return findOne(spec);
        }
        return super.findById(id);
    }

    /*
     * ===================================================================================================
     * Find end
     * ===================================================================================================
     * Exists start
     * ===================================================================================================
     */

    @Override
    public boolean exists(@Nonnull Specification<T> spec) {
        if (read) {
            Specification<T> aclReadSpec = aclExecutor.read();
            if (Objects.nonNull(aclReadSpec)) {
                spec = aclReadSpec.and(spec);
            }
        }
        return super.exists(spec);
    }

    @Override
    public boolean existsById(@Nonnull ID id) {
        if (read) {
            Specification<T> spec = (root, query, criteriaBuilder) -> {
                Path<?> path = root.get(ei.getIdAttribute());
                Predicate idPredicate = criteriaBuilder.equal(path, id);
                return criteriaBuilder.and(idPredicate);
            };
            return exists(spec);
        }
        return super.existsById(id);
    }

    /*
     * ===================================================================================================
     * Exists end
     * ===================================================================================================
     * Save start
     * ===================================================================================================
     */

    @Nonnull
    @Override
    public <S extends T> S save(@Nonnull S entity) {
        if (write) {
            aclExecutor.write();
        }
        return super.save(entity);
    }

    /*
     * ===================================================================================================
     * Save end
     * ===================================================================================================
     * Count start
     * ===================================================================================================
     */

    @Override
    public long count() {
        if (read) {
            return count((Specification<T>) null);
        }
        return super.count();
    }

    /*
     * ===================================================================================================
     * Count end
     * ===================================================================================================
     * Delete start
     * ===================================================================================================
     */

    @Override
    public void deleteById(@Nonnull ID id) {
        if (write) {
            aclExecutor.write();
        }
        if (read) {
            if (!existsById(id)) {
                return;
            }
        }
        super.deleteById(id);
    }

    /**
     * 不校验读权限, 因为此处的entity已经是事务管理的对象, 也就是已经从当前事务中成功查询出来的数据实体
     *
     * @param entity must not be {@literal null}.
     */
    @Override
    public void delete(@Nonnull T entity) {
        super.delete(entity);
    }

    @Override
    public void deleteAllByIdInBatch(@Nonnull Iterable<ID> ids) {
        if (write) {
            aclExecutor.write();
        }
        if (read) {
            Set<ID> set = new HashSet<>();
            for (ID id : ids) {
                if (existsById(id)) {
                    set.add(id);
                }
            }
            super.deleteAllByIdInBatch(set);
        }
        super.deleteAllByIdInBatch(ids);
    }

    /**
     * 这个不做特殊处理, 因为这种删除方式需要先查询, 在session内的实体对象才允许这样删除, 而在查询的时候已经经过ACL过滤
     *
     * @param entities entities to be deleted. Must not be {@literal null}.
     */
    @Override
    public void deleteAllInBatch(@Nonnull Iterable<T> entities) {
        super.deleteAllInBatch(entities);
    }

    /*
     * ===================================================================================================
     * Delete end
     * ===================================================================================================
     */
}
