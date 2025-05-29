package io.github.honhimw.ddd.jimmer.domain;

import io.github.honhimw.core.AuditorDTO;
import io.github.honhimw.util.TypeMapping;
import org.babyfish.jimmer.ImmutableObjects;
import org.mapstruct.Condition;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.SourcePropertyName;

/**
 * @author hon_him
 * @since 2025-03-19
 */

@Mapper(
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface JimmerEntityMapping extends TypeMapping {

    AuditorDTO do2dto(Auditor _do);

    Auditor dto2do(AuditorDTO dto);

    @Condition
    default boolean isLoaded(AggregateRoot _do, @SourcePropertyName String name) {
        return ImmutableObjects.isLoaded(_do, name);
    }

}
