package io.github.honhimw.spring.web.reactive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.util.JsonUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.util.MimeType;

import java.util.TimeZone;

/**
 * @author hon_him
 * @since 2023-04-15
 */

public class WebFluxJackson2Decoder extends AbstractJackson2Decoder {

    public WebFluxJackson2Decoder() {
        this(JsonUtils.mapper().copy()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setTimeZone(TimeZone.getDefault())
            , MediaType.APPLICATION_JSON);
    }

    public WebFluxJackson2Decoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
    }

}
