package io.github.honhimw.spring.data;

import io.github.honhimw.spring.data.jpa.IJpa;
import io.github.honhimw.spring.data.jpa.util.PageUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2024-07-31
 */

@AutoConfigureAfter({
    DataSourceAutoConfiguration.class,
})
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(DataSource.class)
@ConditionalOnClass(value = {
    IJpa.class,
    JpaRepository.class
})
public class ISpringDataJpaAutoConfiguration {

    @Value("${spring.data.web.pageable.one-indexed-parameters:true}")
    private Boolean firstPageNo;

    @PostConstruct
    void setupPageUtils() {
        if (firstPageNo) {
            PageUtils.setFirstPageNo(1);
        } else {
            PageUtils.setFirstPageNo(0);
        }
    }

    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    AuditorAware<?> auditorAware() {
        return Optional::empty;
    }

}
