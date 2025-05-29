package io.github.honhimw.ddd.jpa.util;

import org.hibernate.type.descriptor.java.*;
import org.hibernate.type.descriptor.jdbc.*;
import org.hibernate.type.internal.BasicTypeImpl;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Common used hibernate types definitions
 *
 * @author hon_him
 * @since 2025-03-11
 */

public class TypeDefs {

    public static JavaType java() {
        return JavaType.INSTANCE;
    }

    public static JdbcType jdbc() {
        return JdbcType.INSTANCE;
    }

    public static BasicType basic() {
        return BasicType.INSTANCE;
    }

    public static class JavaType {
        private JavaType() {
        }

        private static final JavaType INSTANCE = new JavaType();

        public StringJavaType str() {
            return StringJavaType.INSTANCE;
        }

        public IntegerJavaType i32() {
            return IntegerJavaType.INSTANCE;
        }

        public LongJavaType i64() {
            return LongJavaType.INSTANCE;
        }

        public BooleanJavaType bool() {
            return BooleanJavaType.INSTANCE;
        }

        public LocalDateTimeJavaType localDateTime() {
            return LocalDateTimeJavaType.INSTANCE;
        }

        public InstantJavaType instant() {
            return InstantJavaType.INSTANCE;
        }
    }

    public static class JdbcType {
        private JdbcType() {
        }

        private static final JdbcType INSTANCE = new JdbcType();

        public VarcharJdbcType varchar() {
            return VarcharJdbcType.INSTANCE;
        }

        public NVarcharJdbcType nvarchar() {
            return NVarcharJdbcType.INSTANCE;
        }

        public LongVarcharJdbcType longVarchar() {
            return LongVarcharJdbcType.INSTANCE;
        }

        public LongNVarcharJdbcType longNVarchar() {
            return LongNVarcharJdbcType.INSTANCE;
        }

        public IntegerJdbcType i32() {
            return IntegerJdbcType.INSTANCE;
        }

        public BigIntJdbcType i64() {
            return BigIntJdbcType.INSTANCE;
        }

        public BooleanJdbcType bool() {
            return BooleanJdbcType.INSTANCE;
        }

        public LocalDateTimeJdbcType localDateTime() {
            return LocalDateTimeJdbcType.INSTANCE;
        }

        public InstantJdbcType instant() {
            return InstantJdbcType.INSTANCE;
        }
    }

    public static class BasicType {
        private final BasicTypeImpl<String> varchar = new BasicTypeImpl<>(JavaType.INSTANCE.str(), JdbcType.INSTANCE.varchar());
        private final BasicTypeImpl<String> nvarchar = new BasicTypeImpl<>(JavaType.INSTANCE.str(), JdbcType.INSTANCE.nvarchar());
        private final BasicTypeImpl<String> longVarchar = new BasicTypeImpl<>(JavaType.INSTANCE.str(), JdbcType.INSTANCE.longVarchar());
        private final BasicTypeImpl<String> longNVarchar = new BasicTypeImpl<>(JavaType.INSTANCE.str(), JdbcType.INSTANCE.longNVarchar());
        private final BasicTypeImpl<Integer> i32 = new BasicTypeImpl<>(JavaType.INSTANCE.i32(), JdbcType.INSTANCE.i32());
        private final BasicTypeImpl<Long> i64 = new BasicTypeImpl<>(JavaType.INSTANCE.i64(), JdbcType.INSTANCE.i64());
        private final BasicTypeImpl<Boolean> bool = new BasicTypeImpl<>(JavaType.INSTANCE.bool(), JdbcType.INSTANCE.bool());
        private final BasicTypeImpl<LocalDateTime> localDateTime = new BasicTypeImpl<>(JavaType.INSTANCE.localDateTime(), JdbcType.INSTANCE.localDateTime());
        private final BasicTypeImpl<Instant> instant = new BasicTypeImpl<>(JavaType.INSTANCE.instant(), JdbcType.INSTANCE.instant());

        private BasicType() {
        }

        private static final BasicType INSTANCE = new BasicType();

        public BasicTypeImpl<String> varchar() {
            return varchar;
        }

        public BasicTypeImpl<String> nvarchar() {
            return nvarchar;
        }

        public BasicTypeImpl<String> longVarchar() {
            return longVarchar;
        }

        public BasicTypeImpl<String> longNVarchar() {
            return longNVarchar;
        }

        public BasicTypeImpl<Integer> i32() {
            return i32;
        }

        public BasicTypeImpl<Long> i64() {
            return i64;
        }

        public BasicTypeImpl<Boolean> bool() {
            return bool;
        }

        public BasicTypeImpl<LocalDateTime> localDateTime() {
            return localDateTime;
        }

        public BasicTypeImpl<Instant> instant() {
            return instant;
        }

    }

}
