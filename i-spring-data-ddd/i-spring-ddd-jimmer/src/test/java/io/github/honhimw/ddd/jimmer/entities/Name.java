package io.github.honhimw.ddd.jimmer.entities;

import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Embeddable;

/**
 * @author honhimW
 * @since 2025-06-26
 */

@Embeddable
public interface Name {

    @Column(name = "first_name")
    String first();

    @Column(name = "last_name")
    String last();

    NestedName nestedName();

}
