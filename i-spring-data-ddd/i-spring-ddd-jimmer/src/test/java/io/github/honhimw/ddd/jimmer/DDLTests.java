package io.github.honhimw.ddd.jimmer;

import io.github.honhimw.ddd.jdbc.JDBCUtils;
import io.github.honhimw.ddd.jimmer.entities.Author;
import io.github.honhimw.ddd.jimmer.entities.AuthorTable;
import io.github.honhimw.ddd.jimmer.entities.BookTable;
import io.github.honhimw.ddd.jimmer.entities.LocationTable;
import io.github.honhimw.ddd.jimmer.entities.CompositeIdDOTable;
import io.github.honhimw.ddd.jimmer.util.Utils;
import io.github.honhimw.ddl.DDLUtils;
import io.github.honhimw.ddl.StandardTableExporter;
import io.github.honhimw.ddl.dialect.DDLDialect;
import io.github.honhimw.util.JsonUtils;
import lombok.SneakyThrows;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TargetLevel;
import org.babyfish.jimmer.sql.EnumType;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.babyfish.jimmer.sql.runtime.SqlFormatter;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author honhimW
 * @since 2025-06-26
 */

public class DDLTests extends InMemoryBaseTest {

    EnumType.Strategy strategy = EnumType.Strategy.NAME;

    @Override
    protected DB using() {
        return DB.H2;
//        return DB.POSTGRESQL;
//        return DB.MYSQL;
//        return DB.ORACLE;
//        return DB.SQL_SERVER;
//        return DB.SQLITE;
//        return DB.TI_DB;
    }

    @Override
    protected JSqlClient.Builder config(JSqlClient.Builder builder) {
        return builder
            .setSqlFormatter(SqlFormatter.PRETTY)
            .setDefaultEnumStrategy(strategy)
            .setDatabaseNamingStrategy(DefaultDatabaseNamingStrategy.LOWER_CASE)
            ;
    }

    @Test
    @SneakyThrows
    void createTable() {
        JSqlClientImplementor client = getClient();
        StandardTableExporter standardTableExporter = new StandardTableExporter(client);
        List<Table<?>> tables = List.of(AuthorTable.$, LocationTable.$, BookTable.$, CompositeIdDOTable.$);
        for (Table<?> table : tables) {
            List<String> sqlCreateStrings = standardTableExporter.getSqlCreateStrings(table);
            List<String> sqlDropStrings = standardTableExporter.getSqlDropStrings(table);
            for (String sqlCreateString : sqlCreateStrings) {
                System.out.println(sqlCreateString);
            }
            for (String sqlDropString : sqlDropStrings) {
                System.out.println(sqlDropString);
            }
            List<Map<String, Object>> result = execute(connection -> {
                for (String createString : sqlCreateStrings) {
                    PreparedStatement preparedStatement = connection.prepareStatement(createString);
                    preparedStatement.execute();
                }
                ResultSet author = connection.getMetaData().getColumns(null, null, table.getImmutableType().getTableName(client.getMetadataStrategy()), null);
                for (String sqlDropString : sqlDropStrings) {
                    PreparedStatement preparedStatement = connection.prepareStatement(sqlDropString);
                    preparedStatement.execute();
                }
                return JDBCUtils.toMap(author);
            });
            Map<String, Map<String, Object>> collect = result.stream().collect(Collectors.toMap(map -> (String) map.get("COLUMN_NAME"), map -> map));
            doAssert(table, collect);
        }
    }

    void doAssert(Table<?> table, Map<String, Map<String, Object>> collect) {
        ImmutableType immutableType = table.getImmutableType();
        Map<String, ImmutableProp> allScalarProps = DDLUtils.allDefinitionProps(immutableType);
        Map<String, ImmutableProp> propMap = allScalarProps.values().stream().collect(Collectors.toMap(prop -> DDLUtils.getName(prop, getClient().getMetadataStrategy()), prop -> prop));
        DDLDialect ddlDialect = DDLDialect.of(dialect, null);
        for (Map.Entry<String, Map<String, Object>> entry : collect.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> value = entry.getValue();
            ImmutableProp prop = propMap.get(key);
            assert prop != null: key;

            if (prop.isReference(TargetLevel.PERSISTENT)) {
                prop = prop.getTargetType().getIdProp();
            }
            int jdbcType = ddlDialect.resolveJdbcType(prop.getReturnClass(), strategy);

            Object dataType = value.get("DATA_TYPE");
            assert dataType != null;

            assert Objects.equals(jdbcType, dataType): "jdbc: %d, dataType: %s".formatted(jdbcType, dataType);
        }
    }

}
