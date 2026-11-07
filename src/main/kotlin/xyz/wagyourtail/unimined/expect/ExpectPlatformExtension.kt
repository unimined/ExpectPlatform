package xyz.wagyourtail.unimined.expect

import groovy.lang.Closure
import groovy.lang.DelegatesTo
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.process.JavaExecSpec
import org.jetbrains.annotations.VisibleForTesting
import xyz.wagyourtail.unimined.expect.TransformPlatform.*
import xyz.wagyourtail.unimined.expect.transform.ExpectPlatformParams
import xyz.wagyourtail.unimined.expect.transform.ExpectPlatformTransform
import xyz.wagyourtail.unimined.expect.utils.FinalizeOnRead
import java.io.File

val Project.expectPlatform: ExpectPlatformExtension
    get() = extensions.getByType(ExpectPlatformExtension::class.java)

abstract class ExpectPlatformExtension(val project: Project) {

    @set:VisibleForTesting
    var version = ExpectPlatformExtension::class.java.`package`.implementationVersion ?: "1.1.0-SNAPSHOT"

    val annotationsDep by lazy { "xyz.wagyourtail.unimined.expect-platform:expect-platform-annotations:$version" }
    val agentDep by lazy { "xyz.wagyourtail.unimined.expect-platform:expect-platform-agent:$version:all" }

    var stripAnnotations by FinalizeOnRead(false)

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
                    it.stripAnnotations.convention(stripAnnotations)
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

    /**
     * Adds the expect-platform agent to a java exec spec.
     * [side] — `client`/`server` — enables agent-side stripping of
     * [xyz.wagyourtail.unimined.expect.annotation.Environment]
     * members for the other side; needed in dev because loaders
     * read env annotations off raw bytes, before agent transforms.
     */
    @JvmOverloads
    fun insertAgent(spec: JavaExecSpec, platformName: String, remap: Map<String, String> = emptyMap(), side: String? = null) {
        spec.jvmArgs(getAgentArgs(platformName, remap, side))
    }

    @JvmOverloads
    fun getAgentArgs(platformName: String, remap: Map<String, String> = emptyMap(), side: String? = null): List<String> {
        val args = mutableListOf(
            "-javaagent:${agentJar.absolutePath}",
            "-D${PROPERTY_PLATFORM}=${platformName}",
            "-D${PROPERTY_REMAP}=${mapToString(remap)}"
        )
        if (side != null) {
            args += "-D${PROPERTY_SIDE}=${side}"
        }
        return args
    }

    val agentJar: File by lazy {
        val config = project.configurations.detachedConfiguration(project.dependencies.create(agentDep))
        config.resolve().first { it.extension == "jar" }
    }

    operator fun invoke(action: ExpectPlatformExtension.() -> Unit) = action(this)

}