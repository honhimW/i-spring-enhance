package io.github.honhimw.ddl;

import io.github.honhimw.ddl.dialect.DDLDialect;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.sql.GeneratedValue;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

import java.sql.DatabaseMetaData;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-07-09
 */

@Slf4j
public class StandardSequenceExporter implements Exporter<ImmutableProp> {

    protected final JSqlClientImplementor client;

    protected final DDLDialect dialect;

    public StandardSequenceExporter(JSqlClientImplementor client) {
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

    public StandardSequenceExporter(JSqlClientImplementor client, DatabaseVersion version) {
        this.client = client;
        this.dialect = DDLDialect.of(client.getDialect(), version);
    }

    @Override
    public List<String> getSqlCreateStrings(ImmutableProp exportable) {
        GeneratedValue annotation = exportable.getAnnotation(GeneratedValue.class);
        if (annotation != null) {
            if (StringUtils.isNotBlank(annotation.sequenceName())) {
                String createSequenceString = dialect.getCreateSequenceString(annotation.sequenceName(), 1, 1);
                return List.of(createSequenceString);
            }
        }
        return List.of();
    }

    @Override
    public List<String> getSqlDropStrings(ImmutableProp exportable) {
        GeneratedValue annotation = exportable.getAnnotation(GeneratedValue.class);
        if (annotation != null) {
            if (StringUtils.isNotBlank(annotation.sequenceName())) {
                String createSequenceString = dialect.getDropSequenceString(annotation.sequenceName());
                return List.of(createSequenceString);
            }
        }
        return List.of();
    }

}
