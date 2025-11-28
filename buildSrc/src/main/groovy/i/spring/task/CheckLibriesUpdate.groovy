package i.spring.task

import groovy.xml.XmlParser
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.internal.artifacts.dependencies.DefaultImmutableVersionConstraint
import org.gradle.api.internal.catalog.DependencyModel
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CheckLibriesUpdate extends DefaultTask {

    @Override
    String getGroup() {
        return 'misc'
    }

    @TaskAction
    void run() {
        def pool = Executors.newFixedThreadPool(4)
        def libsDef = [
                'spring-boot-dependencies'         : buildModel('org.springframework.boot', 'spring-boot-dependencies', 'spring-boot-dependencies'),
                'spring-cloud-dependencies'        : buildModel('org.springframework.cloud', 'spring-cloud-dependencies', 'spring-cloud-dependencies'),
                'spring-cloud-alibaba-dependencies': buildModel('com.alibaba.cloud', 'spring-cloud-alibaba-dependencies', 'spring-cloud-alibaba-dependencies'),
                'springdoc-openapi'                : buildModel('org.springdoc', 'springdoc-openapi-bom', 'springdoc-openapi-bom'),
                'spring-ai-bom'                    : buildModel('org.springframework.ai', 'spring-ai-bom', 'spring-ai-bom'),
                'aws-sdk'                          : buildModel('software.amazon.awssdk', 'bom', 'aws-sdk'),
                'jimmer-bom'                       : buildModel('org.babyfish.jimmer', 'jimmer-bom', 'jimmer-bom'),
                'junit-bom'                        : buildModel('org.junit', 'junit-bom', 'junit-bom'),
        ]
        libsDef.putAll(defInLibs())
        def countDownLatch = new CountDownLatch(libsDef.size())

        def red = '\u001B[31m'
        def green = '\u001B[32m'
        def magenta = '\u001B[35m'
        def cyan = '\u001B[36m'
        def stop = '\u001B[0m'

        def output = new CopyOnWriteArrayList()

        output << "$cyan### Start check libraries update, libraries count: ${libsDef.size()} ###$stop"
        libsDef.each {
            def dependency = it.value
            if (dependency == null) {
                println it
            }
            pool.execute {
                def latestVersion = getLatestVersion(dependency.group, dependency.name)
                if (dependency.version.requiredVersion != latestVersion) {
                    def versionRef = dependency.versionRef.replace '.', '-'
                    output << """$magenta# $dependency.group:$dependency.name$stop
$red- $versionRef = "${dependency.version.requiredVersion}"$stop
$green+ $versionRef = "$latestVersion"$stop"""
                }
                countDownLatch.countDown()
            }
        }
        countDownLatch.await 10, TimeUnit.SECONDS
        output << "$cyan### Finish check libraries update ###$stop"
        pool.shutdown()
        output.forEach { println it }
    }

    DependencyModel buildModel(String group, String name, String versionRef) {
        def libs = project.rootProject.extensions.getByType(VersionCatalogsExtension).named('libs')
        libs.findVersion(versionRef).map {
            return new DependencyModel(
                    group, name, versionRef, new DefaultImmutableVersionConstraint(it.requiredVersion), null
            )
        }.orElse(null)
    }

    Map<String, DependencyModel> defInLibs() {
        def versionCatalogsExtension = project.extensions.getByType(VersionCatalogsExtension)
        for (final def versionCatalog in versionCatalogsExtension) {
            return (versionCatalog.config.libraries as Map<String, DependencyModel>).findAll {
                it.key != 'nacos.config.starter' && it.key != 'nacos.discovery.starter'
            }
        }
        return [:]
    }

    String getLatestVersion(String groupId, String artifactId) {
        def baseUrl = "https://repo1.maven.org/maven2"
        def groupPath = groupId.replace('.', '/')
        def url = "${baseUrl}/${groupPath}/${artifactId}/maven-metadata.xml"

        def response = project.uri(url).toURL().openStream().withCloseable { stream -> return stream.bytes }
        def xmlContent = new XmlParser().parseText(new String(response, 'UTF-8'))
        def latestVersion = xmlContent?.versioning?.latest?.text()
        return latestVersion
    }

}
