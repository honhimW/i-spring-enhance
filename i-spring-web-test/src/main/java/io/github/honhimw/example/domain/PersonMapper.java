package io.github.honhimw.example.domain;

import io.github.honhimw.core.IdRequest;
import io.github.honhimw.core.api.BaseMapper;
import io.github.honhimw.ddd.jpa.domain.mapper.JPAEntityMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Map;

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
public interface PersonMapper extends BaseMapper<Map<String, Map<String, Map<String, Object>>>, IdRequest<String>, PersonDO, PersonDTO> {

    PersonMapper MAPPER = Mappers.getMapper(PersonMapper.class);

    @Override
    default PersonDO create2do(Map<String, Map<String, Map<String, Object>>> request) {
        return new PersonDO();
    }

    @Override
    default void update2do(IdRequest<String> request, PersonDO _do) {

    }

    PersonDTO do2dto(PersonDO _do);

    @Mapping(target = "auditor", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    PersonDO dto2do(PersonDTO dto);

}
