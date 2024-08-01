package io.github.honhimw.spring.data.jpa.domain;

import io.github.honhimw.spring.data.common.LogicDelete;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;

/**
 * @author hon_him
 * @since 2022-10-17
 */
@Getter
@Setter
@ToString(callSuper = true)
@RequiredArgsConstructor
@MappedSuperclass
@SQLRestriction("deleted = false")
public abstract class AbstractLogicDeleteAR<A extends AbstractLogicDeleteAR<A, ID>, ID> extends AbstractAuditAR<A, ID> implements
    LogicDelete {

    @Version
    @Column(
        name = "version"
    )
    private Long version;

    @Column(
        name = "deleted",
        nullable = false
    )
    @Comment("是否删除")
    private Boolean deleted;

    @Override
    public boolean isDeleted() {
        return Objects.nonNull(this.deleted) ? this.deleted : false;
    }

    /**
     * Spring bean propertyDescriptor logic.
     * @since v3
     */
    public Boolean getDeleted() {
        return isDeleted();
    }

    @PrePersist
    protected void prePersist() {
        this.setDeleted(false);
        this.setVersion(1L);
    }

    @SuppressWarnings("unchecked")
    public A logicDelete() {
        A a = (A) this;
        this.setDeleted(true);
        return a;
    }

}
