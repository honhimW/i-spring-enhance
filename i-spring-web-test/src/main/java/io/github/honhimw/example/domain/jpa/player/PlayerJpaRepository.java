package io.github.honhimw.example.domain.jpa.player;

import io.github.honhimw.ddd.jpa.domain.repository.SimpleRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * @author hon_him
 * @since 2025-03-18
 */

//@Repository
@NoRepositoryBean
public interface PlayerJpaRepository extends SimpleRepository<PlayerDO, String> {

    @Override
    Optional<PlayerDO> findById(String s);
}
