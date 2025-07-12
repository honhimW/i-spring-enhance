package io.github.honhimw.ddl;

import io.github.honhimw.ddd.jimmer.JimmerProperties;
import io.github.honhimw.ddd.jimmer.repository.JimmerRepository;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-07-11
 */

@Slf4j
public class DDLAutoRunner implements InitializingBean, DisposableBean {

    private final JSqlClientImplementor client;

    private final Environment environment;

    private final ObjectProvider<JimmerRepository<?, ?>> repositoryProvider;

    private final JimmerProperties.DDLAuto ddlAuto;

    private DatabaseVersion databaseVersion;

    public DDLAutoRunner(JSqlClientImplementor client, Environment environment, ObjectProvider<JimmerRepository<?, ?>> repositoryProvider) {
        this.client = client;
        this.environment = environment;
        this.repositoryProvider = repositoryProvider;
        this.ddlAuto = environment.getProperty("spring.jimmer.ddl-auto", JimmerProperties.DDLAuto.class, JimmerProperties.DDLAuto.NONE);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<ImmutableType> types = repositoryProvider.stream().map(JimmerRepository::type).toList();
        if (!types.isEmpty()) {
            SchemaValidator schemaValidator = new SchemaValidator(client);
            this.databaseVersion = schemaValidator.getDatabaseVersion();
            SchemaValidator.Schemas schemas = schemaValidator.load(types);
            List<ImmutableType> nonExistsTypes = types.stream()
                .filter(immutableType -> !schemas.getTableMap().containsKey(immutableType.getTableName(client.getMetadataStrategy())))
                .toList();
            if (!nonExistsTypes.isEmpty()) {
                SchemaCreator schemaCreator = new SchemaCreator(client, databaseVersion);
                schemaCreator.init();
                switch (ddlAuto) {
                    case CREATE, CREATE_DROP -> {
                        List<String> sqlCreateStrings = schemaCreator.getSqlCreateStrings(nonExistsTypes);
                        if (!sqlCreateStrings.isEmpty()) {
                            client.getConnectionManager().execute(connection -> {
                                try {
                                    for (String sqlCreateString : sqlCreateStrings) {
                                        PreparedStatement preparedStatement = connection.prepareStatement(sqlCreateString);
                                        preparedStatement.execute();
                                    }
                                } catch (Exception e) {
                                    throw new IllegalStateException("schema creation error.", e);
                                }
                                return null;
                            });
                        }
                    }
                    default -> {
                    }
                }
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        List<ImmutableType> types = repositoryProvider.stream().map(JimmerRepository::type).toList();
        if (!types.isEmpty()) {
            SchemaCreator schemaCreator = new SchemaCreator(client, databaseVersion);
            schemaCreator.init();
            switch (ddlAuto) {
                case DROP, CREATE_DROP -> {
                    List<String> sqlDropStrings = schemaCreator.getSqlDropStrings(types);
                    if (!sqlDropStrings.isEmpty()) {
                        client.getConnectionManager().execute(connection -> {
                            try {
                                for (String sqlDropString : sqlDropStrings) {
                                    PreparedStatement preparedStatement = connection.prepareStatement(sqlDropString);
                                    preparedStatement.execute();
                                }
                            } catch (Exception e) {
                                throw new IllegalStateException("schema deletion error.", e);
                            }
                            return null;
                        });
                    }
                }
                default -> {
                }
            }
        }
    }
}
