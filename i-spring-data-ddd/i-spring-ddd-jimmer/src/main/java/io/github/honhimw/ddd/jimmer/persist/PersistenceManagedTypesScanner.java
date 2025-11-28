package io.github.honhimw.ddd.jimmer.persist;

import org.babyfish.jimmer.sql.Embeddable;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.MappedSuperclass;
import org.jspecify.annotations.Nullable;
import org.springframework.core.SpringProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.ClassFormatException;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author honhimW
 * @since 2025-09-05
 */

public final class PersistenceManagedTypesScanner {

    private static final String CLASS_RESOURCE_PATTERN = "/**/*.class";

    private static final String PACKAGE_INFO_SUFFIX = ".package-info";

    private static final String IGNORE_CLASSFORMAT_PROPERTY_NAME = "spring.classformat.ignore";

    private static final boolean shouldIgnoreClassFormatException =
        SpringProperties.getFlag(IGNORE_CLASSFORMAT_PROPERTY_NAME);

    private static final Set<AnnotationTypeFilter> entityTypeFilters = CollectionUtils.newLinkedHashSet(4);

    static {
        entityTypeFilters.add(new AnnotationTypeFilter(Entity.class, false));
        entityTypeFilters.add(new AnnotationTypeFilter(Embeddable.class, false));
        entityTypeFilters.add(new AnnotationTypeFilter(MappedSuperclass.class, false));
    }

    private final ResourcePatternResolver resourcePatternResolver;

    /**
     * Create a new {@code PersistenceManagedTypesScanner} for the given resource loader.
     *
     * @param resourceLoader         the {@code ResourceLoader} to use
     * @since 6.1.4
     */
    public PersistenceManagedTypesScanner(ResourceLoader resourceLoader) {

        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    }


    /**
     * Scan the specified packages and return a {@link PersistenceManagedTypes} that
     * represents the result of the scanning.
     *
     * @param packagesToScan the packages to scan
     * @return the {@link PersistenceManagedTypes} instance
     */
    public PersistenceManagedTypes scan(String... packagesToScan) {
        ScanResult scanResult = new ScanResult();
        for (String pkg : packagesToScan) {
            scanPackage(pkg, scanResult);
        }
        return scanResult.toManagedTypes();
    }

    private void scanPackage(String pkg, ScanResult scanResult) {
        try {
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                             ClassUtils.convertClassNameToResourcePath(pkg) + CLASS_RESOURCE_PATTERN;
            Resource[] resources = this.resourcePatternResolver.getResources(pattern);
            MetadataReaderFactory factory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
            for (Resource resource : resources) {
                try {
                    MetadataReader reader = factory.getMetadataReader(resource);
                    String className = reader.getClassMetadata().getClassName();
                    if (matchesEntityTypeFilter(reader, factory)) {
                        scanResult.managedClassNames.add(className);
                        if (scanResult.persistenceUnitRootUrl == null) {
                            URL url = resource.getURL();
                            if (ResourceUtils.isJarURL(url)) {
                                scanResult.persistenceUnitRootUrl = ResourceUtils.extractJarFileURL(url);
                            }
                        }
                    } else if (className.endsWith(PACKAGE_INFO_SUFFIX)) {
                        scanResult.managedPackages.add(className.substring(0,
                            className.length() - PACKAGE_INFO_SUFFIX.length()));
                    }
                } catch (FileNotFoundException ex) {
                    // Ignore non-readable resource
                } catch (ClassFormatException ex) {
                    if (!shouldIgnoreClassFormatException) {
                        throw new IllegalArgumentException("Incompatible class format in " + resource, ex);
                    }
                } catch (Throwable ex) {
                    throw new IllegalStateException("Failed to read candidate component class: " + resource, ex);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to scan classpath for unlisted entity classes", ex);
        }
    }

    /**
     * Check whether any of the configured entity type filters matches
     * the current class descriptor contained in the metadata reader.
     */
    private boolean matchesEntityTypeFilter(MetadataReader reader, MetadataReaderFactory factory) throws IOException {
        for (TypeFilter filter : entityTypeFilters) {
            if (filter.match(reader, factory)) {
                return true;
            }
        }
        return false;
    }


    private static class ScanResult {

        private final List<String> managedClassNames = new ArrayList<>();

        private final List<String> managedPackages = new ArrayList<>();

        @Nullable
        private URL persistenceUnitRootUrl;

        PersistenceManagedTypes toManagedTypes() {
            return new PersistenceManagedTypes(managedClassNames, managedPackages);
        }
    }

}
