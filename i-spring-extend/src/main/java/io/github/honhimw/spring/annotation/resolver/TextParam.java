package io.github.honhimw.spring.annotation.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hon_him
 * @since 2022-06-06
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface TextParam {

    /**
     * exclude validate specific parameters in json path: "id", "entity.xxx",...
     * <p>
     * <pre>{@code
     * {
     *     "id": "1",              // "id"
     *     "entity": {             // "entity"
     *         "hello": "world",   // "entity.hello"
     *         "property": "any"   // "entity.property"
     *     }
     * }
     * }</pre>
     */
    String[] excludesValidate() default {};

    /**
     * This parameter is used to support the transmission of
     * the HTTP request body as binary data for text-based requests.
     * This feature is disable by default.
     * If enabled, the request header should include ‘content-encoding: gzip’
     * and the request body should be transmitted as binary data.
     * <h2>Request Usage:</h2>
     * <pre>{@code
     * String json = "{'foo':'bar'}";
     * byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
     * byte[] finalBytes = Gzip.compress(bytes);
     *
     * httpUtil.post("http(s)://xxx", configurer -> configurer
     *     .header(HttpHeaders.CONTENT_ENCODING, "gzip")
     *     .body(bodyModel -> bodyModel
     *         .binary(binaryBodyModel -> binaryBodyModel
     *             .bytes(finalBytes, ContentType.APPLICATION_JSON)
     *         )));
     * }</pre>
     *
     * @see org.apache.http.HttpHeaders http request headers containing content-encoding: gzip
     */
    boolean gzip() default false;

}
