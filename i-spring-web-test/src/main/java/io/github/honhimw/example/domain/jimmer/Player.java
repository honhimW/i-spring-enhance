package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.ddd.common.AclDataDomain;
import io.github.honhimw.ddd.jimmer.domain.BaseAR;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.sql.*;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@AclDataDomain("player")
@Entity
@Table(name = "player")
public interface Player extends BaseAR, PlayerDomain {

    @Id
    String id();

    @Nullable
    @ManyToOne
    @JoinColumn(name = "full_name_id", referencedColumnName = "id", foreignKeyType = ForeignKeyType.FAKE)
    @OnDissociate(DissociateAction.LAX)
    Name fullName();

    @Nullable
    SBD sbd();

    @Nullable
    Integer age();

}
