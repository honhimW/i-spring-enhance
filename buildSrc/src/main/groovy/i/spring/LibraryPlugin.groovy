package i.spring


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.plugins.PublishingPlugin

@SuppressWarnings('unused')
class LibraryPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply JavaLibraryPlugin
        project.plugins.apply DistributionPlugin

        project.plugins.apply JavaCfgPlugin
        project.plugins.apply TestingCfgPlugin

        if (!project.rootProject.file('buildSrc/maven-repository.gradle').exists()) {
            project.repositories.mavenLocal()
            project.repositories.mavenCentral()
        }

        project.dependencies { DependencyHandler dep ->
            dep.add JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, 'org.springframework.boot:spring-boot'
            dep.add JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, 'org.jspecify:jspecify'
            dep.add JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, 'org.projectlombok:lombok'
            dep.add JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, 'org.springframework.boot:spring-boot-configuration-processor'
        }

    }

}
