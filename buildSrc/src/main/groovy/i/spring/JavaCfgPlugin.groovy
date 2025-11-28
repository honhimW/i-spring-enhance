package i.spring

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar

import static java.nio.charset.StandardCharsets.UTF_8

class JavaCfgPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.configure(JavaPluginExtension) { java ->
            java.sourceCompatibility = JavaVersion.VERSION_17
            java.targetCompatibility = JavaVersion.VERSION_17
            java.withSourcesJar()
        }
        project.tasks.withType(JavaCompile).configureEach { compile ->
            compile.sourceCompatibility = JavaVersion.VERSION_17
            compile.targetCompatibility = JavaVersion.VERSION_17
            compile.options.encoding = UTF_8.name()
            compile.options.compilerArgs << "-Xlint:deprecation"
        }
        project.tasks.withType(Jar).configureEach { jar ->
            jar.enabled = true
        }

        project.configurations.configureEach {
            it.resolutionStrategy {
                cacheChangingModulesFor 0, 'SECONDS'
                cacheDynamicVersionsFor 0, 'SECONDS'
            }
        }
    }
}
