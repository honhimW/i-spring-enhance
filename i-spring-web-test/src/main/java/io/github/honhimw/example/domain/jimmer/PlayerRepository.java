package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.ddd.jimmer.repository.SoftDeleteRepository;
import org.babyfish.jimmer.sql.ast.impl.Expr;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@Repository
public interface PlayerRepository extends SoftDeleteRepository<Player, String> {

    default List<Player> findAllByAge(Integer age) {
        return findAll((root, query, fetcher) -> Expr.eq(root.get("age"), age));
    }

}
