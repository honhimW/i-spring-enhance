package org.example.config;

import io.github.honhimw.ddd.jpa.acl.AclJpaRepositoryFactoryBean;
import org.example.domain.BuildInData;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author hon_him
 * @since 2024-11-29
 */

@Configuration
@EnableJpaAuditing
@ComponentScan(basePackageClasses = BuildInData.class)
@EntityScan(basePackageClasses = BuildInData.class)
@EnableJpaRepositories(basePackageClasses = BuildInData.class, repositoryFactoryBeanClass = AclJpaRepositoryFactoryBean.class)
public class JpaConfig {
}
