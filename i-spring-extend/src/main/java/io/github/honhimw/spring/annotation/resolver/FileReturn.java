package io.github.honhimw.spring.annotation.resolver;

import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author hon_him
 * @see org.springframework.boot.web.server.Compression#setMimeTypes(String[]) enable compression by setting mime types
 * @since 2024-08-06
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileReturn {

    /**
     * @return Default File Name, with extension. e.g. "export.csv"
     */
    String value();

    Encoding encoding() default Encoding.UTF_8_BOM;

    @Getter
    enum Encoding {
        DEFAULT(Charset.defaultCharset()),
        GBK(Charset.forName("GBK")),
        UTF_8(StandardCharsets.UTF_8),
        UTF_8_BOM(StandardCharsets.UTF_8, new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}),
        UTF_16(StandardCharsets.UTF_16),
        UTF_16_BOM(StandardCharsets.UTF_16, new byte[]{(byte) 0xFE, (byte) 0xFF}),
        UTF_32(Charset.forName("UTF-32")),
        UTF_32_BOM(Charset.forName("UTF-32"), new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF}),
        ;

        private final Charset charset;
        private final byte[] prefix;
        private final byte[] suffix;

        Encoding(Charset charset) {
            this(charset, new byte[0], new byte[0]);
        }

        Encoding(Charset charset, byte[] prefix) {
            this(charset, prefix, new byte[0]);
        }

        Encoding(Charset charset, byte[] prefix, byte[] suffix) {
            this.charset = charset;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public boolean isUnicode() {
            String charsetName = charset.name();
            return "UTF-8".equals(charsetName) ||
                   "UTF-16".equals(charsetName) ||
                   "UTF-32".equals(charsetName);
        }

    }

}
