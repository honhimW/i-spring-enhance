package io.github.honhimw.spring.data.jpa.domain.mapper;

import io.github.honhimw.spring.TypeMapping;
import io.github.honhimw.spring.data.common.AuditorDTO;
import io.github.honhimw.spring.data.jpa.domain.ext.Auditor;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * @author hon_him
 * @since 2022-12-14
 */
@Mapper(
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface JPAEntityMapping extends TypeMapping {

    AuditorDTO do2dto(Auditor _do);

    Auditor dto2do(AuditorDTO dto);

}
