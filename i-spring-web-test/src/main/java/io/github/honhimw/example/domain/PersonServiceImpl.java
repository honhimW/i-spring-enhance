package io.github.honhimw.example.domain;

import io.github.honhimw.core.IResult;
import io.github.honhimw.core.IdRequest;
import io.github.honhimw.core.api.BaseMapper;
import io.github.honhimw.ddd.jpa.domain.repository.SimpleRepository;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.spring.web.common.ddd.BaseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

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

    @PostMapping("/body")
    public IResult<Void> body(@RequestBody T t) {
        System.out.println(t);
        return IResult.ok();
    }

    @PostMapping("/body2")
    public Flux<IResult<T>> body(@TextParam Flux<T> t) {
        System.out.println(t);
        return t.map(IResult::ok);
    }

}
