package io.github.honhimw.ddl;

import io.github.honhimw.ddl.dialect.DDLDialect;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.babyfish.jimmer.meta.EmbeddedLevel;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.meta.MetadataStrategy;
import org.babyfish.jimmer.sql.meta.SingleColumn;
import org.babyfish.jimmer.sql.meta.Storage;
import org.babyfish.jimmer.sql.meta.impl.Storages;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.sql.Types.*;

/**
 * @author honhimW
 * @since 2025-06-30
 */

public class DDLUtils {

    public static String replace(String type, Long length, Integer precision, Integer scale) {
        if (scale != null) {
            type = StringUtils.replaceOnce(type, "$s", scale.toString());
        }
        if (length != null) {
            type = StringUtils.replaceOnce(type, "$l", length.toString());
        }
        if (precision != null) {
            type = StringUtils.replaceOnce(type, "$p", precision.toString());
        }
        return type;
    }

    public static boolean isTemporal(int jdbcType) {
        return switch (jdbcType) {
            case Types.TIME,
                 Types.TIME_WITH_TIMEZONE,
                 Types.DATE,
                 Types.TIMESTAMP,
                 Types.TIMESTAMP_WITH_TIMEZONE -> true;
            default -> false;
        };
    }

    @Nullable
    public static Integer resolveDefaultPrecision(int jdbcType, @Nonnull DDLDialect dialect) {
        Integer precision = null;
        if (isTemporal(jdbcType)) {
            precision = dialect.getDefaultTimestampPrecision(jdbcType);
        }
        if (jdbcType == DECIMAL) {
            precision = dialect.getDefaultDecimalPrecision(jdbcType);
        }
        if (jdbcType == FLOAT) {
            precision = dialect.getDefaultDecimalPrecision(jdbcType);
        }
        if (jdbcType == DOUBLE) {
            precision = dialect.getDefaultDecimalPrecision(jdbcType);
        }
        return precision;
    }

    public static String getName(ImmutableProp prop, MetadataStrategy metadataStrategy) {
        Storage storage = Storages.of(prop, metadataStrategy);
        if (storage instanceof SingleColumn singleColumn) {
            return singleColumn.getName();
        }
        return prop.getName();
    }

    public static Map<String, ImmutableProp> allDefinitionProps(ImmutableType immutableType) {
        Map<String, ImmutableProp> props = new LinkedHashMap<>();
        Map<String, ImmutableProp> selectableScalarProps = immutableType.getSelectableProps();
        selectableScalarProps.forEach((k, v) -> {
            if (v.isEmbedded(EmbeddedLevel.BOTH)) {
                ImmutableType targetType = v.getTargetType();
                Map<String, ImmutableProp> next = allDefinitionProps(targetType);
                next.forEach((nextKey, nextValue) -> props.put(k + '.' + nextKey, nextValue));
            } else {
                props.put(k, v);
            }
        });
        return props;
    }

}
