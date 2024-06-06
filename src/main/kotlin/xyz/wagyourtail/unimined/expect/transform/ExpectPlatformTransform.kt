package xyz.wagyourtail.unimined.expect.transform

import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import xyz.wagyourtail.unimined.expect.TransformPlatform
import xyz.wagyourtail.unimined.expect.utils.openZipFileSystem

abstract class ExpectPlatformTransform : TransformAction<ExpectPlatformParams> {

    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val platformName = parameters.platformName.get()
        val input = inputArtifact.get().asFile
        if (input.isDirectory) {
            val output = outputs.dir(input.name + "-expect-platform")
            TransformPlatform(platformName).transform(input.toPath(), output.toPath())
        } else if (input.extension == "jar") {
            val output = outputs.file(input.nameWithoutExtension + "-expect-platform." + input.extension)
            input.toPath().openZipFileSystem().use { inputFs ->
                output.toPath().openZipFileSystem(mapOf("create" to true)).use { outputFs ->
                    TransformPlatform(platformName).transform(inputFs.getPath("/"), outputFs.getPath("/"))
                }
            }
        } else {
            outputs.file(input)
        }
    }

}