package io.github.honhimw.example.web;

import io.github.honhimw.core.IPageRequest;
import io.github.honhimw.core.IResult;
import io.github.honhimw.core.PageInfoVO;
import io.github.honhimw.ddd.jimmer.domain.AuditorDraft;
import io.github.honhimw.ddd.jimmer.util.IFetcher;
import io.github.honhimw.ddd.jimmer.util.PageUtils;
import io.github.honhimw.example.domain.jimmer.*;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.util.SnowflakeUtils;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.Expression;
import org.babyfish.jimmer.sql.ast.PropExpression;
import org.babyfish.jimmer.sql.ast.impl.Expr;
import org.babyfish.jimmer.sql.ast.mutation.DeleteResult;
import org.babyfish.jimmer.sql.ast.query.ConfigurableRootQuery;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.babyfish.jimmer.sql.ast.tuple.Tuple6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@RestController
@RequestMapping("/jimmer")
public class JimmerController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private NameJimmerRepository nameJimmerRepository;

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
    @GetMapping("/bitwise")
    public IResult<Object> bitwise() {

        ConfigurableRootQuery<PlayerTable, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer>> select = sqlClient.createQuery(PlayerTable.$)
            .where()
            .select(
                Expr.bitwiseXor(PlayerTable.$.age(), 100),
                Expr.bitwiseAnd(PlayerTable.$.age(), 100),
                Expr.bitwiseOr(PlayerTable.$.age(), 100),
                Expr.bitwiseNot(PlayerTable.$.age()),
                Expr.leftShift(PlayerTable.$.age(), 2),
                Expr.rightShift(PlayerTable.$.age(), 2)
            );
        List<Tuple6<Integer, Integer, Integer, Integer, Integer, Integer>> execute = select.execute();
        List<List<Integer>> list = execute.stream().map(tuple -> List.of(
            tuple.get_1(),
            tuple.get_2(),
            tuple.get_3(),
            tuple.get_4(),
            tuple.get_5(),
            tuple.get_6()
        )).toList();
        return IResult.ok(list);
    }

    @Transactional("jimmerTransactionManager")
    @GetMapping("/spec")
    public IResult<Object> spec() {
        List<Player> age = playerRepository.findAll((root, query, fetcher) -> {
            PropExpression.Num<Integer> expression = root.num("age");
            Expr.lt(expression, 22);
            return Expr.and(
                root.join("fullName").get("firstName").eq("Chris"),
//                root.<Integer>num("age").le(22),
                Expr.<Integer>bitwiseNot(root.num("age")).eq(-23)
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
    @PostMapping()
    public IResult<PlayerDTO> create(@TextParam PlayerDTO playerDTO) {

        playerDTO.setId(SnowflakeUtils.getInstance().nextIdStr());
        Player player1 = PlayerMapper.MAPPER.dto2do(playerDTO);
//        player1 = PlayerDraft.$.produce(player1, draft -> draft
//            .setAuditor(AuditorDraft.$.produce(draft1 -> draft1
//                .setCreatedAt(Instant.now())
//                .setCreatedBy("")
//                .setUpdatedAt(Instant.now())
//                .setUpdatedBy("")
//            )));
        Player save = playerRepository.save(player1);
        return IResult.ok(PlayerMapper.MAPPER.do2dto(save));
    }

    @Transactional("jimmerTransactionManager")
    @PutMapping()
    public IResult<Object> update(@TextParam PlayerDTO playerDTO, @RequestParam(value = "delete", defaultValue = "false") boolean delete) {
        String id = playerDTO.getId();
        Player update = playerRepository.update(id, player -> PlayerDraft.$.produce(player, draft -> {
                Integer age = playerDTO.getAge();
                draft
                    .setAge(age)
                    .setDeleted(delete);
            }
        ));
        return IResult.ok(PlayerMapper.MAPPER.do2dto(update));
    }

    @Autowired
    private JSqlClient sqlClient;

    @Transactional("jimmerTransactionManager")
    @DeleteMapping("/{id}")
    public IResult<Void> delete(@PathVariable("id") String id, @RequestParam(value = "soft", defaultValue = "false") boolean soft) {
//        DeleteResult deleteResult = sqlClient.deleteById(Name.class, id);
        if (soft) {
            playerRepository.softDelete(id);
        } else {
            playerRepository.deleteById(id);
        }
        return IResult.ok();
    }

    @GetMapping("/sql")
    public IResult<Object> sql() {
        MutableRootQuery<PlayerTable> query = sqlClient.createQuery(PlayerTable.$);
        ConfigurableRootQuery<PlayerTable, Instant> select = query.select(Expr.sql(Instant.class, "current_timestamp"));
        List<Instant> execute = select.execute();
        return IResult.ok(execute);
    }

}
