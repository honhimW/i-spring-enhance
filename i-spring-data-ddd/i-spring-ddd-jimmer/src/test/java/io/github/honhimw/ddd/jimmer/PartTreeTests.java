package io.github.honhimw.ddd.jimmer;

import lombok.SneakyThrows;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.impl.Expr;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Optional;

/**
 * @author hon_him
 * @since 2025-03-20
 */

public class PartTreeTests {

    @Test
    @SneakyThrows
    void parse() {
        PartTree findByAge = new PartTree("findByAge", Player.class);
        System.out.println(findByAge);
    }

    public interface Player {
        int id();
        int age();
    }

    public interface TestRepository {
        Optional<Player> findByAge(int age);
    }

}
