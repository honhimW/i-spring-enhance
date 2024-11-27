package io.github.honhimw.ddd.jpa.domain.mapper;

import io.github.honhimw.core.AuditorDTO;
import io.github.honhimw.ddd.jpa.domain.ext.Auditor;
import io.github.honhimw.util.TypeMapping;
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
