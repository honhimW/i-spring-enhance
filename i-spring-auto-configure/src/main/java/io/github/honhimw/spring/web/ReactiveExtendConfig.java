package io.github.honhimw.spring.web;

import jakarta.annotation.Nonnull;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.http.codec.json.AbstractJackson2Encoder;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class ReactiveExtendConfig implements WebFluxConfigurer {

    private final AbstractJackson2Decoder decoder;

    private final AbstractJackson2Encoder encoder;

    public ReactiveExtendConfig(AbstractJackson2Decoder decoder, AbstractJackson2Encoder encoder) {
        this.decoder = decoder;
        this.encoder = encoder;
    }

    @Override
    public void configureHttpMessageCodecs(@Nonnull ServerCodecConfigurer configurer) {
        ServerCodecConfigurer.ServerDefaultCodecs serverDefaultCodecs = configurer.defaultCodecs();
        serverDefaultCodecs.jackson2JsonDecoder(decoder);
        serverDefaultCodecs.jackson2JsonEncoder(encoder);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("POST", "GET", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true);
    }

}
