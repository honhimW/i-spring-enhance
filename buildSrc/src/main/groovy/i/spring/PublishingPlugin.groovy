package i.spring


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlatformPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

@SuppressWarnings('unused')
class PublishingPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply MavenPublishPlugin
        def rootProject = project.rootProject
        project.extensions.configure(PublishingExtension) { publishing ->
            project.afterEvaluate {
                def group = rootProject.group.toString()
                def name = project.name
                def ver = project.version == 'unspecified' ? rootProject.version.toString() : project.version.toString()
                def packageKind
                SoftwareComponent target

                project.plugins.withType(JavaLibraryPlugin).configureEach {
                    target = project.components.java
                    packageKind = 'jar'
                }

                project.plugins.withType(JavaPlatformPlugin).configureEach {
                    target = project.components.javaPlatform
                    if (name.contains('bom')) {
                        packageKind = 'pom'
                    } else {
                        packageKind = 'jar'
                    }
                }

                publishing.publications {publication ->
                    publication.create('library', MavenPublication) {mavenPublication ->
                        mavenPublication.versionMapping {vms ->
                            vms.allVariants {
                                it.fromResolutionResult()
                            }
                        }
                        mavenPublication.groupId = group
                        mavenPublication.artifactId = name
                        mavenPublication.version = ver
                        mavenPublication.pom {
                            it.packaging = packageKind
                        }
                        mavenPublication.from target
                    }
                }
            }
        }
        if (project.rootProject.file('buildSrc/maven-repository.gradle').exists()) {
            project.apply from: project.rootProject.file('buildSrc/maven-repository.gradle')
        }
        project.tasks.register('doDeploy') {
            it.group = 'deploy'
            it.dependsOn "${project.path}:publishLibraryPublicationToNexusRepository"
        }
        project.tasks.register('doGenerate') {
            it.group = 'deploy'
            it.dependsOn "${project.path}:publishLibraryPublicationToBuild-dirRepository"
        }
    }

}
