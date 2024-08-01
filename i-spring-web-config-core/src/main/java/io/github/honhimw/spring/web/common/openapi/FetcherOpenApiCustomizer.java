package io.github.honhimw.spring.web.common.openapi;

import io.github.honhimw.spring.web.common.WebConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * @author hon_him
 * @since 2023-05-22
 */

@Component
@ConditionalOnClass(OpenAPI.class)
public class FetcherOpenApiCustomizer implements GlobalOpenApiCustomizer {

    @Override
    public void customise(OpenAPI openApi) {
        openApi.getPaths().forEach((name, pathItem) -> {
            HeaderParameter onlyInclude = new HeaderParameter();
            onlyInclude.name(WebConstants.FETCH_ONLY_INCLUDE);
            onlyInclude.description("e.g. /code;/data/id -> {'code': 0, 'data': {'id': '1'}}");
            onlyInclude.required(false);
            onlyInclude.in("header");
            pathItem.addParametersItem(onlyInclude);
            HeaderParameter nonExclude = new HeaderParameter();
            nonExclude.name(WebConstants.FETCH_NON_EXCLUDE);
            nonExclude.description("e.g. /code;/data/*/id -> {'msg': 'hi', 'data': [{'title': 'hello'} ...] ...}");
            nonExclude.required(false);
            nonExclude.in("header");
            pathItem.addParametersItem(nonExclude);
        });
    }

}
