package io.github.honhimw.test.ddd;

import io.github.honhimw.ddd.jimmer.EnableJimmerRepositories;
import io.github.honhimw.ddd.jimmer.util.IFetcher;
import io.github.honhimw.example.domain.jimmer.*;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.query.Example;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author honhimW
 * @since 2025-05-27
 */

public class ExampleTest extends BaseTest {

    @Override
    protected Map<String, String> properties() {
        Map<String, String> properties = new LinkedHashMap<>();
        // h2
//        properties.put("spring.datasource.class-name", "org.h2.Driver");
//        properties.put("spring.datasource.url", "jdbc:h2:mem:test;MODE\\=PostgreSql;DB_CLOSE_DELAY\\=-1;IGNORECASE\\=FALSE;DATABASE_TO_UPPER\\=FALSE");
//        properties.put("debug", "true");
        properties.put("spring.datasource.class-name", "org.postgresql.Driver");
        properties.put("spring.datasource.url", "jdbc:postgresql://127.0.0.1:5432/tmp");
        properties.put("spring.datasource.username", "postgres");
        properties.put("spring.datasource.password", "testdb");
        return properties;
    }

    @Override
    protected List<Class<?>> userConfig() {
        return List.of(Config.class);
    }

    @Override
    protected void run(AssertableApplicationContext context) throws Exception {
        JSqlClient sqlClient = (JSqlClient) context.getBean("sqlClient");
        PlayerRepository repo = context.getBean(PlayerRepository.class);
        Example<Player> playerExample = Example.of(new PlayerDraft.Builder().age(18).build());
        Predicate eq = PlayerTable.$.eq(playerExample);
        IFetcher<Player> playerIFetcher = IFetcher.of(PlayerTable.$, PlayerFetcher.$);
        playerIFetcher.add("fullName", NameFetcher.$.firstName());
        List<Player> execute = sqlClient.createQuery(PlayerTable.$).where(eq).select(playerIFetcher.toSelection(PlayerTable.$)).execute();
        List<PlayerDTO> list = execute.stream().map(PlayerMapper.MAPPER::do2dto).toList();
        System.out.println(list);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableJimmerRepositories(basePackageClasses = PlayerRepository.class)
    public static class Config {



    }

}
