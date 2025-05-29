package io.github.honhimw.example.domain.jimmer;

import org.babyfish.jimmer.sql.Embeddable;

/**
 * @author hon_him
 * @since 2025-03-12
 */

@Embeddable
public interface SBD {

    int squat();

    int benchPress();

    int deadLift();

}
