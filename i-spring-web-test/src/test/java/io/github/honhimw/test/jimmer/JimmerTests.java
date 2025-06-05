package io.github.honhimw.test.jimmer;

import io.github.honhimw.example.domain.jimmer.Player;
import io.github.honhimw.example.domain.jimmer.PlayerDraft;
import lombok.SneakyThrows;
import org.babyfish.jimmer.ImmutableObjects;
import org.junit.jupiter.api.Test;

/**
 * @author honhimW
 * @since 2025-06-03
 */

public class JimmerTests {

    @Test
    @SneakyThrows
    void isLoad() {
        Player build = new PlayerDraft.Builder().age(17).build();
        System.out.println(ImmutableObjects.isLoaded(build, "id"));
        System.out.println(ImmutableObjects.isLoaded(build, "age"));
    }

}
