package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.ddd.jimmer.domain.AggregateRoot;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.Table;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@Entity
@Table(name = "full_name")
public interface Name extends AggregateRoot {

    @Id
    String id();

    String firstName();

    String lastName();
}
