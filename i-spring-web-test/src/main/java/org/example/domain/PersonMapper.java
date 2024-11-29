package org.example.domain;

import io.github.honhimw.ddd.jpa.domain.mapper.JPAEntityMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
        NameMapper.class
    }
)
public interface PersonMapper {

    PersonMapper MAPPER = Mappers.getMapper(PersonMapper.class);

    PersonDTO do2dto(PersonDO _do);

    @Mapping(target = "auditor", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    PersonDO dto2do(PersonDTO dto);

}
