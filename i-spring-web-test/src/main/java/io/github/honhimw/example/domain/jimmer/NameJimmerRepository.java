package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.ddd.jimmer.domain.SimpleRepository;
import org.springframework.stereotype.Repository;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@Repository
public interface NameJimmerRepository extends SimpleRepository<Name, String> {

}
