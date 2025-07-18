package io.github.honhimw.ddl;

import io.github.honhimw.ddl.annotations.*;
import io.github.honhimw.ddl.annotations.ForeignKey;
import io.github.honhimw.ddl.dialect.DDLDialect;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.Strings;
import org.babyfish.jimmer.meta.EmbeddedLevel;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.GeneratedValue;
import org.babyfish.jimmer.sql.GenerationType;
import org.babyfish.jimmer.sql.meta.MetadataStrategy;
import org.babyfish.jimmer.sql.meta.SingleColumn;
import org.babyfish.jimmer.sql.meta.Storage;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;
import org.babyfish.jimmer.sql.meta.impl.Storages;

import java.lang.annotation.Annotation;
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
            type = Strings.CS.replaceOnce(type, "$s", scale.toString());
        }
        if (length != null) {
            type = Strings.CS.replaceOnce(type, "$l", length.toString());
        }
        if (precision != null) {
            type = Strings.CS.replaceOnce(type, "$p", precision.toString());
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

    public static class DefaultColumnDef implements ColumnDef {

        @Override
        public Nullable nullable() {
            return Nullable.NULL;
        }

        @Override
        public String sqlType() {
            return "";
        }

        @Override
        public int jdbcType() {
            return OTHER;
        }

        @Override
        public long length() {
            return -1;
        }

        @Override
        public int precision() {
            return -1;
        }

        @Override
        public int scale() {
            return -1;
        }

        @Override
        public String comment() {
            return "";
        }

        @Override
        public String definition() {
            return "";
        }

        @Override
        public ForeignKey foreignKey() {
            return null;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ColumnDef.class;
        }
    }

    public static class DefaultForeignKey implements ForeignKey {
        @Override
        public String name() {
            return "";
        }

        @Override
        public String definition() {
            return "";
        }

        @Override
        public OnDeleteAction action() {
            return OnDeleteAction.NONE;
        }

        @Override
        public Class<? extends ConstraintNamingStrategy> naming() {
            return ConstraintNamingStrategy.class;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ForeignKey.class;
        }
    }

    public static class DefaultGeneratedValue implements GeneratedValue {
        @Override
        public GenerationType strategy() {
            return GenerationType.AUTO;
        }

        @Override
        public Class<? extends UserIdGenerator<?>> generatorType() {
            return UserIdGenerator.None.class;
        }

        @Override
        public String generatorRef() {
            return "";
        }

        @Override
        public String sequenceName() {
            return "";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return GeneratedValue.class;
        }
    }

    public static class DefaultTableDef implements TableDef {
        @Override
        public Unique[] uniques() {
            return new Unique[0];
        }

        @Override
        public Index[] indexes() {
            return new Index[0];
        }

        @Override
        public String comment() {
            return "";
        }

        @Override
        public Check[] checks() {
            return new Check[0];
        }

        @Override
        public String tableType() {
            return "";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return TableDef.class;
        }
    }

    public static abstract class DefaultUnique implements Unique {
        @Override
        public String name() {
            return "";
        }

        @Override
        public Kind kind() {
            return Kind.PATH;
        }

        @Override
        public Class<? extends ConstraintNamingStrategy> naming() {
            return ConstraintNamingStrategy.class;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Unique.class;
        }
    }

}
