package org.example.domain;

import io.github.honhimw.ddd.jpa.domain.mapper.JPAEntityMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author hon_him
 * @since 2023-04-11
 */

@Mapper(
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {
        JPAEntityMapping.class,
    }
)
public interface NameMapper {

    NameMapper MAPPER = Mappers.getMapper(NameMapper.class);

    NameDTO do2dto(NameDO _do);

    NameDO dto2do(NameDTO dto);

}
