package org.example.domain;

import io.github.honhimw.ddd.jpa.domain.repository.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * @author hon_him
 * @since 2024-11-29
 */

@Repository
public interface PersonRepository extends BaseRepository<PersonDO, String> {
}
