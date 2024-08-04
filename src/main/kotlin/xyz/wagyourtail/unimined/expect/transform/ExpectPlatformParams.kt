package xyz.wagyourtail.unimined.expect.transform

import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

interface ExpectPlatformParams : TransformParameters {

    @get:Input
    val platformName: Property<String>

    /**
     * same values as with SimpleRemapper, since that's what it's passed to.
     * This is necessary to set for hooking up @Environment.
     */
    @get:Input
    @get:Optional
    val remap: MapProperty<String, String>

    @get:Input
    @get:Optional
    val stripAnnotations: Property<Boolean>

}