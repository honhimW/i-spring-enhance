package i.spring


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension

class UsingPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def using = project.configurations.create('using')
        using.setCanBeResolved false
        using.setCanBeConsumed false
        using.setCanBeDeclared true
        project.plugins.withType(JavaPlugin).configureEach {
            project.extensions.getByType(JavaPluginExtension).sourceSets.configureEach { ss ->
                project.configurations.named(ss.compileClasspathConfigurationName).configure { it.extendsFrom using }
                project.configurations.named(ss.runtimeClasspathConfigurationName).configure { it.extendsFrom using }
                project.configurations.named(ss.annotationProcessorConfigurationName).configure { it.extendsFrom using }
                project.configurations.named(ss.compileOnlyConfigurationName).configure {
                    it.extendsFrom project.configurations.named(ss.annotationProcessorConfigurationName).get()
                }
            }
        }
    }
}
