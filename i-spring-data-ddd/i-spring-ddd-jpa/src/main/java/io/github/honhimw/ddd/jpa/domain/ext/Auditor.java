package io.github.honhimw.ddd.jpa.domain.ext;

import io.github.honhimw.ddd.common.Value;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * @author hon_him
 * @since 2022-11-01
 */
@Embeddable
@Getter
@Setter
public class Auditor implements Value<Auditor> {

    @Column(
        name = "created_at",
        updatable = false,
        nullable = false
    )
    @Comment("created at")
    @CreatedDate
    private Instant createdAt;

    @Column(
        name = "updated_at",
        nullable = false
    )
    @Comment("updated at")
    @LastModifiedDate
    private Instant updatedAt;

    @Column(
        name = "created_by",
        updatable = false
    )
    @Comment("created by")
    @CreatedBy
    private String createdBy;

    @Column(
        name = "updated_by"
    )
    @Comment("updated by")
    @LastModifiedBy
    private String updatedBy;

}
