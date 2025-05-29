package io.github.honhimw.ddd.jimmer;

import io.github.honhimw.ddd.jimmer.repository.JimmerRepositoriesRegistrarForAnnotation;
import io.github.honhimw.ddd.jimmer.repository.JimmerRepositoryFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author hon_him
 * @since 2025-01-13
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(JimmerRepositoriesRegistrarForAnnotation.class)
public @interface EnableJimmerRepositories {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
     * {@code @EnableJimmerRepositories("org.my.pkg")} instead of
     * {@code @EnableJimmerRepositories(basePackages="org.my.pkg")}.
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
     * attribute. Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
     * package of each class specified will be scanned. Consider creating a special no-op marker class or interface in
     * each package that serves no purpose other than being referenced by this attribute.
     */
    Class<?>[] basePackageClasses() default {};

    ComponentScan.Filter[] includeFilters() default {};

    /**
     * Specifies which types are not eligible for component scanning.
     */
    ComponentScan.Filter[] excludeFilters() default {};

    /**
     * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
     */
    Class<?> repositoryFactoryBeanClass() default JimmerRepositoryFactoryBean.class;

    /**
     * Configures the name of the JSqlClient/KSqlClient bean definition to be
     * used to create repositories discovered through this annotation.
     * Defaults to sqlClient.
     */
    String sqlClientRef() default "sqlClient";

    /**
     * Configures the location of where to find the Spring Data named queries properties file. Will default to
     * {@code META-INF/jpa-named-queries.properties}.
     */
    String namedQueriesLocation() default "";

    /**
     * Returns the postfix to be used when looking up custom repository implementations. Defaults to {@literal Impl}. So
     * for a repository named {@code PersonRepository} the corresponding implementation class will be looked up scanning
     * for {@code PersonRepositoryImpl}.
     */
    String repositoryImplementationPostfix() default "Impl";

}
