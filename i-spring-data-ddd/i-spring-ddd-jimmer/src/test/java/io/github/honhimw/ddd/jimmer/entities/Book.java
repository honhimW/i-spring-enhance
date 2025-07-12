package io.github.honhimw.ddd.jimmer.entities;

import io.github.honhimw.ddd.jimmer.domain.BaseAR;
import io.github.honhimw.ddl.annotations.*;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.sql.*;

import java.util.List;

/**
 * @author honhimW
 * @since 2025-06-26
 */

@Entity
@Table(name = Book.TABLE_NAME)
@TableDef(
    indexes = {
        @Index(columns = {"name"}),
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
    @JoinColumn(name = "author_id", referencedColumnName = "id", foreignKeyType = ForeignKeyType.REAL)
    @OnDissociate(DissociateAction.LAX)
    @ColumnDef(foreignKey = @ForeignKey(action = OnDeleteAction.CASCADE))
    Author author();

    @ManyToMany
    @MiddleTable(
        comment = "书_出版社关联",
        joinColumnForeignKey = @ForeignKey(name = "fk_bi", action =  OnDeleteAction.CASCADE),
        inverseJoinColumnForeignKey = @ForeignKey(action = OnDeleteAction.CASCADE)
    )
    List<PublishingHouse> publishingHouses();


}
