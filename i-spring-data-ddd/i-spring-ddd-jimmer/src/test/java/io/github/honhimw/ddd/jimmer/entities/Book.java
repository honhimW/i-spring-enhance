package io.github.honhimw.ddd.jimmer.entities;

import io.github.honhimw.ddd.jimmer.domain.BaseAR;
import io.github.honhimw.ddl.annotations.Index;
import io.github.honhimw.ddl.annotations.TableDef;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.sql.*;

/**
 * @author honhimW
 * @since 2025-06-26
 */

@Entity
@Table(name = Book.TABLE_NAME)
@TableDef(
    indexes = {
        @Index(name = "idx_name", columns = {"name"}),
    },
    comment = "书"
)
public interface Book extends BaseAR {

    String TABLE_NAME = "book";

    @Id
    @GeneratedValue
    int id();

    @Nullable
    String name();

    @Nullable
    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "id", foreignKeyType = ForeignKeyType.FAKE)
    @OnDissociate(DissociateAction.LAX)
    Author author();

}
