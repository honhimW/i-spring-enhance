package io.github.honhimw.ddl.dialect;

import io.github.honhimw.ddl.DatabaseVersion;
import org.babyfish.jimmer.sql.dialect.TiDBDialect;

/**
 * @author honhimW
 * @since 2025-06-27
 */

public class TiDBDDLDialect extends MySqlDDLDialect {

    public TiDBDDLDialect() {
        this(null);
    }

    public TiDBDDLDialect(final DatabaseVersion version) {
        super(new TiDBDialect(), version);
    }

    @Override
    public boolean needsStartingValue() {
        return true;
    }

    @Override
    public String getCreateSequenceString(String sequenceName, int initialValue, int incrementSize) {
        return getCreateSequenceString(sequenceName)
               + " start with " + initialValue
               + " increment by " + incrementSize
               + startingValue(initialValue, incrementSize);
    }

}
