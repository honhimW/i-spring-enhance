package io.github.honhimw.ddd.jimmer.entities;

import io.github.honhimw.ddd.jimmer.domain.BaseAR;
import io.github.honhimw.jddl.anno.*;
import org.jspecify.annotations.Nullable;
import org.babyfish.jimmer.sql.*;

import java.util.List;

/**
 * @author honhimW
 * @since 2025-07-10
 */

@Entity
@Table(name = PublishingHouse.TABLE_NAME)
@TableDef(
    comment = "出版社"
)
public interface PublishingHouse extends BaseAR {

    String TABLE_NAME = "publishing_house";

    @Id
    @GeneratedValue
    int id();

    @Nullable
    String name();

    @ManyToMany(mappedBy = "publishingHouses")
    List<Book> books();


}
