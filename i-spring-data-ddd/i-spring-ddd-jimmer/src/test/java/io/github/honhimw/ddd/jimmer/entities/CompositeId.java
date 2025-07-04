package io.github.honhimw.ddd.jimmer.entities;

import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Embeddable;

/**
 * @author honhimW
 * @since 2025-06-26
 */

@Embeddable
public interface CompositeId {

    @Column(name = "fid")
    String first();

    @Column(name = "sid")
    String second();

    @Column(name = "tid")
    String third();

}
