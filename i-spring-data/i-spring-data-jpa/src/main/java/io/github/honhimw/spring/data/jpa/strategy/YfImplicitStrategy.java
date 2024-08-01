package io.github.honhimw.spring.data.jpa.strategy;

import io.github.honhimw.spring.data.jpa.util.NamingUtils;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitForeignKeyNameSource;
import org.hibernate.boot.model.naming.ImplicitIndexNameSource;
import org.hibernate.boot.model.naming.ImplicitUniqueKeyNameSource;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;

/**
 * @author hon_him
 * @since 2022-10-18
 */

public class YfImplicitStrategy extends SpringImplicitNamingStrategy {

    @Override
    public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
        Identifier userProvidedIdentifier = source.getUserProvidedIdentifier();
        return userProvidedIdentifier != null ? userProvidedIdentifier : toIdentifier(
            NamingUtils.of(source.getBuildingContext().getBuildingOptions().getSchemaCharset()).genName(
                "unq_",
                source.getTableName(),
                source.getColumnNames()
            ),
            source.getBuildingContext()
        );
    }

    @Override
    public Identifier determineIndexName(ImplicitIndexNameSource source) {
        Identifier userProvidedIdentifier = source.getUserProvidedIdentifier();
        return userProvidedIdentifier != null ? userProvidedIdentifier : toIdentifier(
            NamingUtils.of(source.getBuildingContext().getBuildingOptions().getSchemaCharset()).genName(
                "idx_",
                source.getTableName(),
                source.getColumnNames()
            ),
            source.getBuildingContext()
        );
    }

    @Override
    public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
        Identifier userProvidedIdentifier = source.getUserProvidedIdentifier();
        return userProvidedIdentifier != null ? userProvidedIdentifier : toIdentifier(
            NamingUtils.of(source.getBuildingContext().getBuildingOptions().getSchemaCharset()).genName(
                "fk_",
                source.getTableName(),
                source.getColumnNames()
            ),
            source.getBuildingContext()
        );
    }
}
