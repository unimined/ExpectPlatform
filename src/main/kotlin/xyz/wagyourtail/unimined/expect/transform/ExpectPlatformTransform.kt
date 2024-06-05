package xyz.wagyourtail.unimined.expect.transform

import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import xyz.wagyourtail.unimined.expect.TransformPlatform
import xyz.wagyourtail.unimined.expect.utils.openZipFileSystem
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists
import kotlin.io.path.outputStream

abstract class ExpectPlatformTransform : TransformAction<ExpectPlatformParams> {

    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val input = inputArtifact.get().asFile
        if (input.isDirectory) {
            val output = outputs.dir(input.name + "-expect-platform")
            TransformPlatform.expectPlatform(input.toPath(), output.toPath(), parameters.platformName.get())
        } else if (input.extension == "jar") {
            val output = outputs.file(input.nameWithoutExtension + "-expect-platform." + input.extension)
            input.toPath().openZipFileSystem().use { inputFs ->
                output.toPath().openZipFileSystem(mapOf("create" to true)).use { outputFs ->
                    TransformPlatform.expectPlatform(inputFs.getPath("/"), outputFs.getPath("/"), parameters.platformName.get())
                }
            }
        } else {
            outputs.file(input)
        }
    }

}