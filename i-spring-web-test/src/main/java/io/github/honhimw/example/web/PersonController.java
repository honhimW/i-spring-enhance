package io.github.honhimw.example.web;

import io.github.honhimw.core.IPageRequest;
import io.github.honhimw.core.IResult;
import io.github.honhimw.core.PageInfoVO;
import io.github.honhimw.ddd.jpa.util.PageUtils;
import io.github.honhimw.example.domain.jpa.PersonDO;
import io.github.honhimw.example.domain.jpa.PersonDTO;
import io.github.honhimw.example.domain.jpa.PersonMapper;
import io.github.honhimw.example.domain.jpa.PersonRepository;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hon_him
 * @since 2024-11-29
 */

@RestController
@RequestMapping("/person")
public class PersonController {

    @Autowired
    private PersonRepository personRepository;

    @PostMapping("/list")
    public IResult<PageInfoVO<PersonDTO>> list(@TextParam IPageRequest<PersonDTO> iPageRequest) {
        IPageRequest<PersonDO> personDOIPageRequest = PageUtils.convertRequest(iPageRequest, PersonMapper.MAPPER::dto2do);
        Page<PersonDO> paging = PageUtils.paging(personRepository, personDOIPageRequest);
        PageInfoVO<PersonDTO> personDTOPageInfoVO = PageUtils.pageInfoVO(paging, PersonMapper.MAPPER::do2dto);
        return IResult.ok(personDTOPageInfoVO);
    }

}
