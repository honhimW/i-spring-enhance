package io.github.honhimw.ddd.jpa.domain;

import io.github.honhimw.ddd.common.DomainEntity;
import io.github.honhimw.ddd.jpa.domain.ext.Auditor;
import jakarta.persistence.Embedded;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author hon_him
 * @since 2022-10-17
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public abstract class AbstractAuditAR<A extends AbstractAuditAR<A, ID>, ID> extends AbstractAR<A, ID> implements
    DomainEntity<A, ID> {

    @Embedded
    private Auditor auditor = new Auditor();

}
