package io.github.honhimw.spring.extend;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * @author honhimW
 * @since 2025-11-24
 */

@Getter
@Setter
@ConfigurationProperties(prefix = IExtendProperties.PREFIX)
public class IExtendProperties implements Serializable {

    public static final String PREFIX = "i.spring";

    private Json json;

    @Getter
    @Setter
    public static class Json implements Serializable {
        /**
         * json configuration
         */
        private boolean enabled = true;
    }

}
