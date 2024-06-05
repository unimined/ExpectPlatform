package xyz.wagyourtail.unimined.expect

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Internal
import xyz.wagyourtail.unimined.expect.transform.ExpectPlatformTransform

abstract class ExpectPlatformExtension(val project: Project) {
    @get:Internal
    val version = ExpectPlatformExtension::class.java.`package`.implementationVersion ?: "1.0-SNAPSHOT"

    val annotationsDep = "xyz.wagyourtail.unimined:expect-platform:$version:annotations"


    fun platform(platformName: String, configuration: Configuration) {
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
                }
            }
        }

        configuration.attributes {
            it.attribute(expectPlatformAttribute, true)
        }
    }

}