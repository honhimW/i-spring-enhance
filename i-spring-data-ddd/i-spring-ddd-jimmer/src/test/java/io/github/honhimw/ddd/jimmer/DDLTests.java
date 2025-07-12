package io.github.honhimw.ddd.jimmer;

import io.github.honhimw.ddd.jdbc.JDBCUtils;
import io.github.honhimw.ddd.jimmer.entities.*;
import io.github.honhimw.ddl.DDLUtils;
import io.github.honhimw.ddl.SchemaCreator;
import io.github.honhimw.ddl.SchemaValidator;
import io.github.honhimw.ddl.annotations.ColumnDef;
import io.github.honhimw.ddl.annotations.ForeignKey;
import io.github.honhimw.ddl.annotations.Unique;
import io.github.honhimw.ddl.dialect.DDLDialect;
import io.github.honhimw.ddl.fake.FakeImmutablePropImpl;
import io.github.honhimw.ddl.fake.FakeImmutableTypeImpl;
import io.github.honhimw.util.JsonUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TargetLevel;
import org.babyfish.jimmer.sql.EnumType;
import org.babyfish.jimmer.sql.GeneratedValue;
import org.babyfish.jimmer.sql.GenerationType;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.impl.table.TableTypeProvider;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.babyfish.jimmer.sql.runtime.SqlFormatter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.LinkedHashMap;
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
    void init() {
        super.init();
        Logger logger = LoggerFactory.getLogger(SchemaCreator.class);
        if (logger instanceof ch.qos.logback.classic.Logger log) {
            log.setLevel(ch.qos.logback.classic.Level.DEBUG);
        }
    }

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
        SchemaCreator schemaCreator = new SchemaCreator(client);
        schemaCreator.init();
        List<Table<?>> tables = List.of(AuthorTable.$, LocationTable.$, BookTable.$, CompositeIdDOTable.$, PublishingHouseTable.$);
        List<ImmutableType> types = tables.stream().map(TableTypeProvider::getImmutableType).toList();
        List<String> sqlCreateStrings = schemaCreator.getSqlCreateStrings(types);
        execute(connection -> {
            for (String createString : sqlCreateStrings) {
                PreparedStatement preparedStatement = connection.prepareStatement(createString);
                preparedStatement.execute();
            }
            return null;
        });
        for (ImmutableType immutableType : types) {
            List<Map<String, Object>> result = execute(connection -> {
                ResultSet resultSet = connection.getMetaData().getColumns(null, null, immutableType.getTableName(client.getMetadataStrategy()), null);
                return JDBCUtils.toMap(resultSet);
            });
            Map<String, Map<String, Object>> collect = result.stream().collect(Collectors.toMap(map -> (String) map.get("COLUMN_NAME"), map -> map));
            doAssert(immutableType, collect);
        }
        List<String> sqlDropStrings = schemaCreator.getSqlDropStrings(types);
        execute(connection -> {
            for (String dropString : sqlDropStrings) {
                PreparedStatement preparedStatement = connection.prepareStatement(dropString);
                preparedStatement.execute();
            }
            return null;
        });
    }

    void doAssert(ImmutableType immutableType, Map<String, Map<String, Object>> collect) {
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

            jdbcType = adjustJdbcType(jdbcType);
            assert Objects.equals(jdbcType, dataType): "prop: %s, jdbc: %d, dataType: %s".formatted(prop.getName(), jdbcType, dataType);
        }
    }

    int adjustJdbcType(int jdbcType) {
        return switch (using()) {
            case ORACLE -> switch (jdbcType) {
                case Types.BOOLEAN, Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT, Types.DECIMAL -> Types.NUMERIC;
                case Types.DOUBLE ->  Types.FLOAT;
                default -> jdbcType;
            };
            case SQL_SERVER -> switch (jdbcType) {
                case Types.DOUBLE ->  Types.FLOAT;
                default -> jdbcType;
            };
            default -> jdbcType;
        };
    }

    @Test
    @SneakyThrows
    void fakeTypeCreation() {
        JSqlClientImplementor client = getClient();
        SchemaCreator schemaCreator = new SchemaCreator(client);
        schemaCreator.init();

        FakeImmutableTypeImpl fakeImmutableType = new FakeImmutableTypeImpl();
        fakeImmutableType.tableName = "middle_table";
        fakeImmutableType.props = new LinkedHashMap<>();
        FakeImmutablePropImpl id = new FakeImmutablePropImpl();
        id.name = "id";
        id.returnClass = Integer.TYPE;
        id.isId = true;
        id.isColumnDefinition = true;
        id.annotations = new Annotation[] {new DDLUtils.DefaultGeneratedValue()};

        FakeImmutablePropImpl aid = new FakeImmutablePropImpl();
        aid.name = "aid";
        aid.returnClass = String.class;
        aid.isColumnDefinition = true;
        aid.annotations = new Annotation[] {
            new DDLUtils.DefaultForeignKey(),
        };

        FakeImmutablePropImpl bid = new FakeImmutablePropImpl();
        bid.name = "bid";
        bid.returnClass = String.class;
        bid.isColumnDefinition = true;
        bid.annotations = new Annotation[] {
            new DDLUtils.DefaultForeignKey(),
        };

        fakeImmutableType.tableDef = new DDLUtils.DefaultTableDef() {
            @Override
            public Unique[] uniques() {
                return new Unique[] {
                    new DDLUtils.DefaultUnique() {
                        @Override
                        public String[] columns() {
                            return new String[] {"aid", "bid"};
                        }
                    }
                };
            }

            @Override
            public String comment() {
                return "假表";
            }
        };
        fakeImmutableType.javaClass = Object.class;
        fakeImmutableType.idProp = id;
        fakeImmutableType.props.put(id.name, id);
        fakeImmutableType.props.put(aid.name, aid);
        fakeImmutableType.props.put(bid.name, bid);
        fakeImmutableType.selectableProps = fakeImmutableType.props;

        List<String> sqlCreateStrings = schemaCreator.getSqlCreateStrings(List.of(fakeImmutableType));
        for (String sqlCreateString : sqlCreateStrings) {
            System.out.println(sqlCreateString);
        }
    }

    @Test
    @SneakyThrows
    void bitwise() {
        JSqlClientImplementor client = getClient();
        SchemaValidator schemaValidator = new SchemaValidator(client);
        System.out.println(schemaValidator.getDatabaseVersion());
        List<Table<?>> tables = List.of(AuthorTable.$, LocationTable.$, BookTable.$, CompositeIdDOTable.$, PublishingHouseTable.$);
        List<ImmutableType> types = tables.stream().map(TableTypeProvider::getImmutableType).toList();
        SchemaValidator.Schemas load = schemaValidator.load(types);
        System.out.println(JsonUtils.toPrettyJson(load));
    }

//    @Override
//    protected DataSource newDataSource() {
//        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setDriverClassName("org.postgresql.Driver");
//        dataSource.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/tmp");
//        dataSource.setUsername("postgres");
//        dataSource.setPassword("testdb");
//        return dataSource;
//    }
}
