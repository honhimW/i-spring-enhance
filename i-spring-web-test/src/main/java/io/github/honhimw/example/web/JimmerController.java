package io.github.honhimw.example.web;

import io.github.honhimw.core.IPageRequest;
import io.github.honhimw.core.IResult;
import io.github.honhimw.core.PageInfoVO;
import io.github.honhimw.ddd.jimmer.domain.Auditor;
import io.github.honhimw.ddd.jimmer.domain.AuditorDraft;
import io.github.honhimw.ddd.jimmer.util.IFetcher;
import io.github.honhimw.ddd.jimmer.util.PageUtils;
import io.github.honhimw.example.domain.jimmer.*;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.util.SnowflakeUtils;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.impl.Expr;
import org.babyfish.jimmer.sql.ast.query.ConfigurableRootQuery;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@RestController
@RequestMapping("/jimmer")
public class JimmerController {

    @Autowired
    private PlayerRepository playerRepository;

    @GetMapping("/hello")
    public IResult<Object> hello() {
        List<Player> all = playerRepository.findAll(IFetcher.of(PlayerTable.$, PlayerFetcher.$).allFields().getDelegate());
        Player produce = PlayerDraft.$.produce(draft -> draft.setAge(22).setId("2"));
//        Player data = playerRepository.findOne(Example.of(produce, ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.STARTING))).get();
        Player data = playerRepository.findBy(Example.of(produce, ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.STARTING)), FluentQuery.FetchableFluentQuery::first).get();
        return IResult.ok(data);
    }

    @PostMapping("/paging")
    public IResult<PageInfoVO<PlayerDTO>> paging(@TextParam IPageRequest<PlayerDTO> pageRequest) {
        IPageRequest<Player> playerIPageRequest = PageUtils.convertRequest(pageRequest, PlayerMapper.MAPPER::dto2do);
        Page<Player> paging = PageUtils.paging(playerRepository, playerIPageRequest);
        PageInfoVO<PlayerDTO> playerDTOPageInfoVO = PageUtils.pageInfoVO(paging, PlayerMapper.MAPPER::do2dto);
        return IResult.ok(playerDTOPageInfoVO);
    }

    @Transactional("jimmerTransactionManager")
    @GetMapping("/spec")
    public IResult<Object> spec() {
        List<Player> age = playerRepository.findAll((root, query, fetcher) -> {
            return Expr.and(
                root.join("fullName").get("firstName").eq("Chris"),
                root.<Integer>num("age").le(22)
            );
        });
        List<PlayerDTO> list = age.stream().map(PlayerMapper.MAPPER::do2dto).toList();
        Player build = new PlayerDraft.Builder().id(SnowflakeUtils.getInstance().nextIdStr()).age(17)
            .fullName(new NameDraft.Builder().id("1").build())
            .sbd(new SBDDraft.Builder().squat(1).benchPress(2).deadLift(3).build())
            .auditor(new AuditorDraft.Builder()
                .createdAt(Instant.now())
                .createdBy("")
                .updatedAt(Instant.now())
                .updatedBy("")
                .build())
            .build();
        playerRepository.save(build);
        return IResult.ok(list);
    }

    @Transactional("jimmerTransactionManager")
    @GetMapping("/update")
    public IResult<Object> update() {
        Player update = playerRepository.update("1", player -> PlayerDraft.$.produce(player, draft -> draft
            .setAge(ThreadLocalRandom.current().nextInt(16, 30))
        ));
        return IResult.ok(PlayerMapper.MAPPER.do2dto(update));
    }

    @Autowired
    private JSqlClient sqlClient;

    @GetMapping("/sql")
    public IResult<Object> sql() {
        MutableRootQuery<PlayerTable> query = sqlClient.createQuery(PlayerTable.$);
        ConfigurableRootQuery<PlayerTable, Instant> select = query.select(Expr.sql(Instant.class, "current_timestamp"));
        List<Instant> execute = select.execute();
        return IResult.ok(execute);
    }

}
