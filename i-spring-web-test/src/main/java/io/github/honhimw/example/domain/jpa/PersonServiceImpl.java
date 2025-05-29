package io.github.honhimw.example.domain.jpa;

import io.github.honhimw.core.IResult;
import io.github.honhimw.core.IdRequest;
import io.github.honhimw.core.api.BaseMapper;
import io.github.honhimw.ddd.jpa.domain.repository.SimpleRepository;
import io.github.honhimw.ddd.jpa.expression.FragmentBuilder;
import io.github.honhimw.ddd.jpa.expression.FragmentExpression;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.spring.web.common.ddd.BaseImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-12-04
 */

@Primary
@Service
public class PersonServiceImpl<T> extends BaseImpl<Map<String, Map<String, Map<String, Object>>>, IdRequest<String>, String, PersonDO, PersonDTO> {

    @Autowired
    private PersonRepository personRepository;

    @Override
    protected SimpleRepository<PersonDO, String> getRepository() {
        return personRepository;
    }

    @Override
    protected BaseMapper<Map<String, Map<String, Map<String, Object>>>, IdRequest<String>, PersonDO, PersonDTO> getMapper() {
        return PersonMapper.MAPPER;
    }

    @Autowired
    private EntityManager em;

    @RequestMapping("/body")
    public IResult<Object> body(@TextParam T t) {
        {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
            {
                FragmentExpression<Integer> str = FragmentBuilder.of(cb).i32("(1 + 1 * 4)");
                tupleQuery.multiselect(str);
            }
//            {
//                tupleQuery.multiselect(cb.sum(cb.literal(1), cb.literal(2)));
//            }
            TypedQuery<Tuple> query = em.createQuery(tupleQuery);
            List<Tuple> resultList = query.getResultList();
            if (!resultList.isEmpty()) {
                List<Object[]> list = resultList.stream().map(Tuple::toArray).toList();
                return IResult.ok(list);
            }
        }
        List<PersonDO> all = personRepository.findAll((root, query, cb) -> {
            FragmentExpression fragmentExpression = new FragmentExpression("(1 + 1)", cb);
            Expression<?> id = FragmentBuilder.of(cb).append("(").append("(1 + 1)").append(" & ").append(root.get("id")).append(")").build();
            return cb.equal(id, 1);
//            return cb.equal(fragmentExpression, cb.prod(cb.literal(1), 2));
//            return cb.equal(cb.diff(root.get("age"), 1), 1);
        });

        System.out.println(t);
        return IResult.ok(all);
    }

    @RequestMapping("/body2")
    public Flux<IResult<T>> body(@TextParam Flux<T> t) {
        System.out.println(t);
        return t.map(IResult::ok);
    }

}
