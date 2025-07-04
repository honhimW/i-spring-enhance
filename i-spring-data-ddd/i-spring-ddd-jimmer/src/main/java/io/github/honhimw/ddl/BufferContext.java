package io.github.honhimw.ddl;

import io.github.honhimw.ddl.annotations.TableDef;
import lombok.Getter;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author honhimW
 * @since 2025-07-03
 */

public class BufferContext {

    public final StringBuilder buf;

    public final JSqlClientImplementor client;

    public final Table<?> table;

    public final ImmutableType tableType;

    @Getter
    private Map<String, ImmutableProp> allDinfitionProps;

    @Getter
    private List<ImmutableProp> scalarProps;

    public final String tableName;

    public final List<String> commentStatements;

    private TableDef tableDef;

    public BufferContext(JSqlClientImplementor client, Table<?> table) {
        this.buf = new StringBuilder();
        this.client = client;
        this.table = table;
        this.tableType = table.getImmutableType();
        this.tableName = tableType.getTableName(client.getMetadataStrategy());
        this.commentStatements = new ArrayList<>();
        this.init();
    }

    private void init() {
        this.scalarProps = new ArrayList<>();
        this.allDinfitionProps = DDLUtils.allDefinitionProps(tableType);
        this.scalarProps.addAll(this.allDinfitionProps.values());
//        Map<String, ImmutableProp> selectableScalarProps = tableType.getSelectableScalarProps();
//        for (Map.Entry<String, ImmutableProp> entry : selectableScalarProps.entrySet()) {
//            ImmutableProp prop = entry.getValue();
//            if (prop.isEmbedded(EmbeddedLevel.BOTH)) {
//                ImmutableType targetType = prop.getTargetType();
//                Map<String, ImmutableProp> allScalarProps = Utils.allScalarProps(targetType);
//                scalarProps.addAll(allScalarProps.values());
//            } else {
//                scalarProps.add(prop);
//            }
//        }
        Class<?> javaClass = tableType.getJavaClass();
        if (javaClass.isAnnotationPresent(TableDef.class)) {
            tableDef = javaClass.getAnnotation(TableDef.class);
        }
    }

    public Optional<TableDef> getTableDef() {
        return Optional.ofNullable(tableDef);
    }

}
