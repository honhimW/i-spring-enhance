package io.github.honhimw.spring.web.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.annotation.Nonnull;
import org.springframework.core.IResolvableTypeSupports;
import org.springframework.core.ResolvableType;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.util.MimeType;

import java.util.Map;

/**
 * @author hon_him
 * @since 2023-04-15
 */

public class WebFluxJackson2Decoder extends AbstractJackson2Decoder {

    public WebFluxJackson2Decoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
    }

    @Nonnull
    @Override
    protected ObjectReader customizeReader(ObjectReader reader, @Nonnull ResolvableType elementType, Map<String, Object> hints) {
        return reader.forType(IResolvableTypeSupports.resolve(elementType).getType());
    }

}
