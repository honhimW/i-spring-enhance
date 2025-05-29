package io.github.honhimw.example.domain.jimmer;

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
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SBDMapper {

    SBDMapper MAPPER = Mappers.getMapper(SBDMapper.class);

    SBDDTO do2dto(SBD sbd);

    SBD dto2do(SBDDTO sbdDTO);

}
