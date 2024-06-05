package xyz.wagyourtail.unimined.expect

import org.gradle.api.Plugin
import org.gradle.api.Project

class ExpectPlatformPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply(mapOf("plugin" to "java"))
        target.extensions.create("expectPlatform", ExpectPlatformExtension::class.java, target)
    }

}
