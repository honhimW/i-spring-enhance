package i.spring

import i.spring.task.CheckLibriesUpdate
import org.gradle.api.Plugin
import org.gradle.api.Project

class MiscPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.tasks.register('checkLibrariesUpdate', CheckLibriesUpdate)
    }
}
