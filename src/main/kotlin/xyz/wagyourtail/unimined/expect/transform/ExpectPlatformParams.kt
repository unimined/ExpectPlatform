package xyz.wagyourtail.unimined.expect.transform

import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface ExpectPlatformParams : TransformParameters {

    @get:Input
    val platformName: Property<String>

}