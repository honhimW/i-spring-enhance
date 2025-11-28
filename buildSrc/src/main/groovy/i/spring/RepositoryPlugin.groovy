package i.spring

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

@SuppressWarnings('unused')
class RepositoryPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply MavenPublishPlugin
        if (project.rootProject.file('buildSrc/maven-repository.gradle').exists()) {
            project.apply from: project.rootProject.file('buildSrc/maven-repository.gradle')
        } else {
            project.repositories.mavenLocal()
            project.repositories.mavenCentral()
        }
    }

}
