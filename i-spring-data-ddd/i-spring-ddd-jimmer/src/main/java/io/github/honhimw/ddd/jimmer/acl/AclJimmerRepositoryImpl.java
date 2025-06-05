package io.github.honhimw.ddd.jimmer.acl;

import io.github.honhimw.ddd.common.AclDataDomain;
import io.github.honhimw.ddd.common.ResourceMod;
import io.github.honhimw.ddd.jimmer.domain.Specification;
import io.github.honhimw.ddd.jimmer.mapping.JimmerEntityInformation;
import io.github.honhimw.ddd.jimmer.repository.SimpleJimmerRepository;
import io.github.honhimw.ddd.jimmer.util.IFetcher;
import io.github.honhimw.ddd.jimmer.util.IProps;
import org.babyfish.jimmer.Input;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.mutation.BatchEntitySaveCommand;
import org.babyfish.jimmer.sql.ast.mutation.MutableDelete;
import org.babyfish.jimmer.sql.ast.mutation.SimpleEntitySaveCommand;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author honhimW
 * @since 2025-05-30
 */

public class AclJimmerRepositoryImpl<E, ID> extends SimpleJimmerRepository<E, ID> {

    private final AclExecutor aclExecutor;
    private final String dataDomain;
    private final boolean read;
    private final boolean write;
    private final boolean execute;

    public AclJimmerRepositoryImpl(JimmerEntityInformation<E, ID> information, JSqlClientImplementor sqlClient, AclProvider aclProvider) {
        super(information, sqlClient);
        Class<?> javaType = entityType();
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
        this.aclExecutor = aclProvider.getExecutor(sqlClient, this.table, this.dataDomain, mod);
    }

    /*
     * ===================================================================================================
     * Specification extension ACL start
     * ===================================================================================================
     */

    @Override
    protected Predicate getPredicate(Specification.Query spec, IProps root, MutableRootQuery<?> query, IFetcher<?> fetcher) {
        if (read) {
            Specification.Query aclReadSpec = aclExecutor.read();
            if (Objects.nonNull(aclReadSpec)) {
                if (Objects.nonNull(spec)) {
                    spec = aclReadSpec.and(spec);
                } else {
                    spec = aclReadSpec;
                }
            }
        }
        return super.getPredicate(spec, root, query, fetcher);
    }

    @Override
    protected Predicate getPredicate(Specification.Delete spec, IProps root, MutableDelete delete) {
        if (execute) {
            Specification.Delete aclDeleteSpec = aclExecutor.delete();
            if (Objects.nonNull(aclDeleteSpec)) {
                if (Objects.nonNull(spec)) {
                    spec = aclDeleteSpec.and(spec);
                } else {
                    spec = aclDeleteSpec;
                }
            }
        }
        return super.getPredicate(spec, root, delete);
    }

    /*
     * ===================================================================================================
     * Specification extension ACL end
     * ===================================================================================================
     * Find start
     * ===================================================================================================
     */



    /*
     * ===================================================================================================
     * Find end
     * ===================================================================================================
     * Exists start
     * ===================================================================================================
     */



    /*
     * ===================================================================================================
     * Exists end
     * ===================================================================================================
     * Save start
     * ===================================================================================================
     */

    @Override
    public @NotNull <S extends E> BatchEntitySaveCommand<S> saveEntitiesCommand(@NotNull Iterable<S> entities) {
        if (write) {
            aclExecutor.write();
        }
        return super.saveEntitiesCommand(entities);
    }

    @Override
    public @NotNull <S extends E> SimpleEntitySaveCommand<S> saveCommand(@NotNull S entity) {
        if (write) {
            aclExecutor.write();
        }
        return super.saveCommand(entity);
    }

    @Override
    public @NotNull SimpleEntitySaveCommand<E> saveCommand(@NotNull Input<E> input) {
        if (write) {
            aclExecutor.write();
        }
        return super.saveCommand(input);
    }

    /*
     * ===================================================================================================
     * Save end
     * ===================================================================================================
     * Count start
     * ===================================================================================================
     */



    /*
     * ===================================================================================================
     * Count end
     * ===================================================================================================
     * Delete start
     * ===================================================================================================
     */



    /*
     * ===================================================================================================
     * Delete end
     * ===================================================================================================
     */

}
