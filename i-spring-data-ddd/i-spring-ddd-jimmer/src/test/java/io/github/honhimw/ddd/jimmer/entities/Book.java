package io.github.honhimw.ddd.jimmer.entities;

import io.github.honhimw.ddd.jimmer.domain.BaseAR;
import io.github.honhimw.jddl.anno.*;
import org.babyfish.jimmer.sql.*;
import org.jspecify.annotations.Nullable;

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
    @ColumnDef(foreignKey = @Relation(action = OnDeleteAction.CASCADE))
    Author author();

    @ManyToMany
    @MiddleTable(
        tableDef = @TableDef(comment = "书_出版社关联"),
        joinColumnForeignKey = @Relation(name = "fk_bi", action =  OnDeleteAction.CASCADE),
        inverseJoinColumnForeignKey = @Relation(action = OnDeleteAction.CASCADE)
    )
    List<PublishingHouse> publishingHouses();


}
