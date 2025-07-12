package io.github.honhimw.ddl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.POJONode;
import io.github.honhimw.ddd.jdbc.JDBCUtils;
import jakarta.annotation.Nullable;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import reactor.util.annotation.NonNull;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author honhimW
 * @since 2025-07-11
 */

public class SchemaValidator {

    private final JSqlClientImplementor client;

    public SchemaValidator(JSqlClientImplementor client) {
        this.client = client;
    }

    private DatabaseVersion databaseVersion;


    public DatabaseVersion getDatabaseVersion() {
        if (databaseVersion == null) {
            databaseVersion = client.getConnectionManager().execute(connection -> {
                try {
                    DatabaseMetaData metaData = connection.getMetaData();
                    int databaseMajorVersion = metaData.getDatabaseMajorVersion();
                    int databaseMinorVersion = metaData.getDatabaseMinorVersion();
                    String databaseProductVersion = metaData.getDatabaseProductVersion();
                    return new DatabaseVersion(databaseMajorVersion, databaseMinorVersion, databaseProductVersion);
                } catch (Exception e) {
                    throw new IllegalStateException("cannot get database version", e);
                }
            });
        }
        return databaseVersion;
    }

    public Schemas load(@NonNull Collection<ImmutableType> immutableTypes) {
        return client.getConnectionManager().execute(connection -> {
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                List<Table> tables = new ArrayList<>();
                for (ImmutableType immutableType : immutableTypes) {
                    String tableSchema = client.getMetadataStrategy().getSchemaStrategy().tableSchema(immutableType);
                    String tableName = immutableType.getTableName(client.getMetadataStrategy());
                    ResultSet table = metaData.getTables(null, tableSchema, tableName, null);
                    List<Map<String, Object>> result = JDBCUtils.toMap(table);
                    if (CollectionUtils.isNotEmpty(result)) {
                        Map<String, Object> first = result.getFirst();
                        List<Column> columns = new ArrayList<>();
                        ArrayNode columnResult = JDBCUtils.toNode(metaData.getColumns(null, tableSchema, tableName, null));
                        for (JsonNode jsonNode : columnResult) {

                            columns.add(
                                new Column(
                                    getNullable(jsonNode.at("/TABLE_CAT"), JsonNode::asText),
                                    getNullable(jsonNode.at("/TABLE_SCHEM"), JsonNode::asText),
                                    getNullable(jsonNode.at("/TABLE_NAME"), JsonNode::asText),
                                    getNullable(jsonNode.at("/COLUMN_NAME"), JsonNode::asText),
                                    getNullable(jsonNode.at("/DATA_TYPE"), JsonNode::asInt),
                                    getNullable(jsonNode.at("/TYPE_NAME"), JsonNode::asText),
                                    getNullable(jsonNode.at("/COLUMN_SIZE"), JsonNode::asInt),
                                    getNullable(jsonNode.at("/DECIMAL_DIGITS"), JsonNode::asInt),
                                    getNullable(jsonNode.at("/NUM_PREC_RADIX"), JsonNode::asInt),
                                    getNullable(jsonNode.at("/NULLABLE"), JsonNode::asInt),
                                    getNullable(jsonNode.at("/REMARKS"), JsonNode::asText),
                                    getNullable(jsonNode.at("/COLUMN_DEF"), JsonNode::asText),
                                    getNullable(jsonNode.at("/CHAR_OCTET_LENGTH"), JsonNode::asInt),
                                    getNullable(jsonNode.at("/ORDINAL_POSITION"), JsonNode::asInt),
                                    getNullable(jsonNode.at("/IS_NULLABLE"), JsonNode::asText),
                                    getNullable(jsonNode.at("/SCOPE_CATALOG"), JsonNode::asText),
                                    getNullable(jsonNode.at("/SCOPE_SCHEMA"), JsonNode::asText),
                                    getNullable(jsonNode.at("/SCOPE_TABLE"), JsonNode::asText),
                                    getNullable(jsonNode.at("/SOURCE_DATA_TYPE"), JsonNode::shortValue),
                                    getNullable(jsonNode.at("/IS_AUTOINCREMENT"), JsonNode::asText),
                                    getNullable(jsonNode.at("/IS_GENERATEDCOLUMN"), JsonNode::asText)
                                )
                            );
                        }
                        tables.add(new Table(
                            (String) first.get("TABLE_CAT"),
                            (String) first.get("TABLE_SCHEM"),
                            (String) first.get("TABLE_NAME"),
                            (String) first.get("TABLE_TYPE"),
                            (String) first.get("REMARKS"),
                            (String) first.get("TYPE_CAT"),
                            (String) first.get("TYPE_SCHEM"),
                            (String) first.get("TYPE_NAME"),
                            (String) first.get("SELF_REFERENCING_COL_NAME"),
                            (String) first.get("REF_GENERATION"),
                            columns
                        ));

                    }
                }
                return new Schemas(tables);
            } catch (Exception e) {
                throw new IllegalStateException("cannot get tables", e);
            }
        });

    }

    public record Table(String tableCatalog, String tableSchema, String tableName, String tableType, String remarks,
                        String typeCatalog, String typeSchema, String typeName, String selfReferencingColName,
                        String refGeneration, List<Column> columns) {

    }

    public record Column(String tableCatalog, String tableSchema, String tableName, String columnName, Integer dataType,
                         String typeName, Integer columnSize, Integer decimalDigits, Integer numPrecisionRadix,
                         Integer nullable, String remarks, String columnDef, Integer charOctetLength,
                         Integer ordinalPosition, String isNullable, String scopeCatalog, String scopeSchema,
                         String scopeTable, Short sourceDataType, String isAutoincrement, String isGeneratedColumn) {

    }

    public static class Schemas {
        @Getter
        private final List<Table> tables;
        @Getter
        private final Map<String, Table> tableMap;

        private Schemas(List<Table> tables) {
            this.tables = tables;
            this.tableMap = tables.stream().collect(Collectors.toMap(Table::tableName, table -> table));
        }

        @Nullable
        public Table get(String tableName) {
            return tableMap.get(tableName);
        }


    }

    private <R> R getNullable(JsonNode node, Function<JsonNode, R> function) {
        if (node.isNull() || node.isMissingNode()) {
            return null;
        } else {
            if (node.isPojo() && node instanceof POJONode pojoNode) {
                if (pojoNode.getPojo() == null) {
                    return null;
                }
            }
            return function.apply(node);
        }
    }

}
