package io.github.honhimw.example.domain.jpa;

import io.github.honhimw.ddd.jpa.domain.repository.SimpleRepository;
import org.springframework.stereotype.Repository;

/**
 * @author hon_him
 * @since 2024-11-29
 */

@Repository
public interface NameRepository extends SimpleRepository<NameDO, String> {
}
