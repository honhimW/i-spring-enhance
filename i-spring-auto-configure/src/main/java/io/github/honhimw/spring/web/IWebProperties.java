package io.github.honhimw.spring.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * @author hon_him
 * @since 2023-06-28
 */

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = IWebProperties.PREFIX)
public class IWebProperties implements Serializable {

    public static final String PREFIX = "i.spring.web";

    private Boolean fallbackHandlerPrintStacktrace = true;

    private Boolean healthyCheckPoint = true;

}
