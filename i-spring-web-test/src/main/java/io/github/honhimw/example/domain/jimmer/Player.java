package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.ddd.jimmer.domain.AuditAR;
import io.github.honhimw.ddd.jimmer.domain.SoftDeleteAR;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.sql.*;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@Entity
@Table(name = "player")
public interface Player extends SoftDeleteAR, AuditAR {

    @Id
    String id();

    @Nullable
    @ManyToOne
    @JoinColumn(name = "full_name_id", referencedColumnName = "id")
    Name fullName();

    @Nullable
    SBD sbd();

    @Nullable
    Integer age();

}
