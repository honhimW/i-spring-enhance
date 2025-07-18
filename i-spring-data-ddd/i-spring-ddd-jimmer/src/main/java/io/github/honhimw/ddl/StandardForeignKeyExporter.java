package io.github.honhimw.ddl;

import io.github.honhimw.ddl.annotations.OnDeleteAction;
import io.github.honhimw.ddl.dialect.DDLDialect;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

import java.sql.DatabaseMetaData;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-07-09
 */

@Slf4j
public class StandardForeignKeyExporter implements Exporter<ForeignKey> {

    protected final JSqlClientImplementor client;

    protected final DDLDialect dialect;

    public StandardForeignKeyExporter(JSqlClientImplementor client) {
        this.client = client;
        DatabaseVersion databaseVersion = client.getConnectionManager().execute(connection -> {
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                int databaseMajorVersion = metaData.getDatabaseMajorVersion();
                int databaseMinorVersion = metaData.getDatabaseMinorVersion();
                String databaseProductVersion = metaData.getDatabaseProductVersion();
                return new DatabaseVersion(databaseMajorVersion, databaseMinorVersion, databaseProductVersion);
            } catch (Exception e) {
                log.warn("cannot get database version, using latest as default", e);
                return new DatabaseVersion(Integer.MAX_VALUE, Integer.MAX_VALUE, "unknown");
            }
        });
        this.dialect = DDLDialect.of(client.getDialect(), databaseVersion);
    }

    public StandardForeignKeyExporter(JSqlClientImplementor client, DatabaseVersion version) {
        this.client = client;
        this.dialect = DDLDialect.of(client.getDialect(), version);
    }

    @Override
    public List<String> getSqlCreateStrings(ForeignKey exportable) {
        String sourceTableName = exportable.table.getTableName(client.getMetadataStrategy());
        String targetTableName = exportable.referencedTable.getTableName(client.getMetadataStrategy());

        StringBuilder buf = new StringBuilder();
        buf.append("alter table ");
        if (dialect.supportsIfExistsAfterAlterTable()) {
            buf.append("if exists ");
        }
        buf.append(sourceTableName);

        String joinColumnName = DDLUtils.getName(exportable.joinColumn, client.getMetadataStrategy());
        String foreignKeyName = getForeignKeyName(exportable);
        if (StringUtils.isNotBlank(exportable.foreignKey.definition())) {
            buf.append(" add constraint ")
                .append(dialect.quote(foreignKeyName))
                .append(' ')
                .append(exportable.foreignKey.definition());
        } else {
            buf.append(" add constraint ")
                .append(dialect.quote(foreignKeyName))
                .append(" foreign key (")
                .append(joinColumnName)
                .append(')')
                .append(" references ")
                .append(targetTableName)
                .append(" (")
                .append(DDLUtils.getName(exportable.referencedTable.getIdProp(), client.getMetadataStrategy()))
                .append(')');
        }
        OnDeleteAction action = exportable.foreignKey.action();
        if (action != OnDeleteAction.NONE) {
            buf.append(" on delete ").append(action.sql);
        }
        return List.of(buf.toString());
    }

    @Override
    public List<String> getSqlDropStrings(ForeignKey exportable) {
        StringBuilder buf = new StringBuilder();
        buf.append("alter table ");
        if (dialect.supportsIfExistsAfterAlterTable()) {
            buf.append("if exists ");
        }
        buf
            .append(exportable.table.getTableName(client.getMetadataStrategy()))
            .append(' ')
            .append(dialect.getDropForeignKeyString())
            .append(' ');
        if (dialect.supportsIfExistsBeforeConstraintName()) {
            buf.append("if exists ");
        }
        buf.append(dialect.quote(getForeignKeyName(exportable)));
        return List.of(buf.toString());
    }

    protected String getForeignKeyName(ForeignKey exportable) {
        String sourceTableName = exportable.table.getTableName(client.getMetadataStrategy());
        String foreignKeyName = exportable.foreignKey.name();
        String joinColumnName = DDLUtils.getName(exportable.joinColumn, client.getMetadataStrategy());
        if (StringUtils.isBlank(foreignKeyName)) {
            try {
                ConstraintNamingStrategy ns = exportable.foreignKey.naming().getConstructor().newInstance();
                foreignKeyName = ns.determineForeignKeyName(sourceTableName, new String[]{joinColumnName});
            } catch (Exception e) {
                throw new IllegalArgumentException("NamingStrategy doesn't have a no-arg constructor");
            }
        }
        return foreignKeyName;
    }

}
