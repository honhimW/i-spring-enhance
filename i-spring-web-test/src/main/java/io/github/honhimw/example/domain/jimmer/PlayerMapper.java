package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.ddd.jimmer.domain.JimmerEntityMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * @author hon_him
 * @since 2025-03-18
 */

@Mapper(
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        JimmerEntityMapping.class,
        NameMapper.class,
        SBDMapper.class,
    }
)
public interface PlayerMapper {

    PlayerMapper MAPPER = Mappers.getMapper(PlayerMapper.class);

    PlayerDTO do2dto(Player _do);

    Player dto2do(PlayerDTO _dto);

}
