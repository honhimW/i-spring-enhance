package io.github.honhimw.example.web;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.honhimw.core.IResult;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author honhimW
 * @since 2025-10-28
 */

@Controller
@RequestMapping("/xml")
public class XmlController {

    @ResponseBody
    @GetMapping(value = "/get.xml", produces = "application/xml")
    public IResult<?> index(@TextParam JsonNode jsonNode) {
        return IResult.ok(jsonNode);
    }

}
