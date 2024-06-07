package xyz.wagyourtail.unimined.expect

import groovy.lang.Closure
import groovy.lang.DelegatesTo
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.process.JavaExecSpec
import xyz.wagyourtail.unimined.expect.transform.ExpectPlatformParams
import xyz.wagyourtail.unimined.expect.transform.ExpectPlatformTransform

abstract class ExpectPlatformExtension(val project: Project) {
    @get:Internal
    val version = ExpectPlatformExtension::class.java.`package`.implementationVersion ?: "1.0.0-SNAPSHOT"

    val annotationsDep = "xyz.wagyourtail.unimined.expect-platform:expect-platform:$version:annotations"
    val agentDep = "xyz.wagyourtail.unimined.expect-platform:expect-platform:$version:agent"

    @JvmOverloads
    fun platform(platformName: String, configuration: Configuration, action: ExpectPlatformParams.() -> Unit = {}) {
        val expectPlatformAttribute = Attribute.of("expectPlatform.${configuration.name}", Boolean::class.javaObjectType)

        project.dependencies.apply {
            attributesSchema {
                it.attribute(expectPlatformAttribute)
            }

            artifactTypes.getByName("jar") {
                it.attributes.attribute(expectPlatformAttribute, false)
            }
//            artifactTypes.getByName("java-classes-directory") {
//                it.attributes.attribute(expectPlatformAttribute, false)
//            }

            registerTransform(ExpectPlatformTransform::class.java) { spec ->
                spec.from.attribute(expectPlatformAttribute, false)
                spec.to.attribute(expectPlatformAttribute, true)

                spec.parameters {
                    it.platformName.set(platformName)
                    it.action()
                }
            }
        }

        configuration.attributes {
            it.attribute(expectPlatformAttribute, true)
        }
    }

    fun platform(
        platformName: String,
        configuration: Configuration,
        @DelegatesTo(
            ExpectPlatformParams::class,
            strategy = Closure.DELEGATE_FIRST
        ) action: Closure<*>
    ) {
        platform(platformName, configuration) {
            action.delegate = this
            action.resolveStrategy = Closure.DELEGATE_FIRST
            action.call()
        }
    }

    @JvmOverloads
    fun insertAgent(spec: JavaExecSpec, platformName: String, remap: Map<String, String> = emptyMap()) {
        spec.jvmArgs("-javaagent:${agentJar.absolutePath}", "-Dexpect.platform=${platformName}", "-Dexpect.remap=${TransformPlatform.mapToString(remap)}")
    }

    val agentJar by lazy {
        val config = project.configurations.detachedConfiguration(project.dependencies.create(agentDep))
        config.resolve().first { it.extension == "jar" }
    }

}