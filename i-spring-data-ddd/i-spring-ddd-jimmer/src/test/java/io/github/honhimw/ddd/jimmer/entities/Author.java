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
@Table(name = Author.TABLE_NAME)
@TableDef(
    indexes = {
        @Index(name = "idx_age_gender", columns = {"age", "gender"}),
        @Index(name = "idx_pen_name", columns = {"name.nestedName.penName"}),
    },
    uniques = {
        @Unique(
            name = "uk_first_name_age",
            columns = {"name.first", "name.nestedName.official", "age"}
        ),
        @Unique(
            name = "uk_last_name_email",
            columns = {"last_name", "email"}, kind = Kind.NAME
        ),
    },
    checks = {
        @Check(name = "update_time_must_later_than_create_time", constraint = "created_at <= updated_at"),
        @Check(name = "created_by_1", constraint = "created_by = '1'"),
    },
    comment = "作者"
)
public interface Author extends BaseAR {

    String TABLE_NAME = "author";

    @Id
    int id();

    @Nullable
    Name name();

    @Key
    @Nullable
    @ColumnDef(length = 3)
    Integer age();

    @Nullable
    @ColumnDef(length = 50, comment = "邮箱", nullable = ColumnDef.Nullable.FALSE)
    String email();

    //    @Nullable
    @Column(name = "gen der")
    Gender gender();

    @Nullable
    @ManyToOne
    @JoinColumn(name = "location", referencedColumnName = "id", foreignKeyType = ForeignKeyType.FAKE)
    @OnDissociate(DissociateAction.LAX)
    Location location();

    @OneToMany(mappedBy = "author")
    List<Book> books();

}
