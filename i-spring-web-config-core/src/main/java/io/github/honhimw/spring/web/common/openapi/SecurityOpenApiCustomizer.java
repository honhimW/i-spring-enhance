package io.github.honhimw.spring.web.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2023-05-22
 */

@Component
@ConditionalOnClass(OpenAPI.class)
@ConditionalOnDefaultWebSecurity
public class SecurityOpenApiCustomizer implements GlobalOpenApiCustomizer {

    @Override
    public void customise(OpenAPI openApi) {
        List<SecurityRequirement> security = openApi.getSecurity();
        if (!CollectionUtils.isNotEmpty(security) || security.stream().noneMatch(securityRequirement -> securityRequirement.containsKey(HttpHeaders.AUTHORIZATION))) {
            SecurityRequirement securityItem = new SecurityRequirement();
            securityItem.addList(HttpHeaders.AUTHORIZATION);
            openApi.addSecurityItem(securityItem);
        }

        Components components = openApi.getComponents();
        Map<String, SecurityScheme> securitySchemes = components.getSecuritySchemes();
        if (!MapUtils.isNotEmpty(securitySchemes) || !securitySchemes.containsKey(HttpHeaders.AUTHORIZATION)) {
            components.addSecuritySchemes(HttpHeaders.AUTHORIZATION, new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(HttpHeaders.AUTHORIZATION));
        }
    }

}
