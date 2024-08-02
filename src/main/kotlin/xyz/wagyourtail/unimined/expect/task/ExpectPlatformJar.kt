@file:Suppress("LeakingThis")

package xyz.wagyourtail.unimined.expect.task

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import xyz.wagyourtail.unimined.expect.utils.FinalizeOnRead
import xyz.wagyourtail.unimined.expect.utils.MustSet
import xyz.wagyourtail.unimined.expect.ExpectPlatformExtension
import xyz.wagyourtail.unimined.expect.TransformPlatform
import xyz.wagyourtail.unimined.expect.transform.ExpectPlatformParams
import xyz.wagyourtail.unimined.expect.utils.openZipFileSystem

abstract class ExpectPlatformJar : Jar(), ExpectPlatformParams {

    private val ep by lazy {
        project.extensions.getByType(ExpectPlatformExtension::class.java)
    }

    @get:InputFiles
    var inputFiles: FileCollection by FinalizeOnRead(MustSet())

    @TaskAction
    fun doTransform() {
        val transformer = TransformPlatform(platformName.get(), remap.get(), stripAnnotations.get())
        for (input in inputFiles) {
            if (input.isDirectory) {
                val output = temporaryDir.resolve(input.name + "-expect-platform")
                transformer.transform(input.toPath(), output.toPath())
                from(output)
            } else if (input.extension == "jar") {
                val output = temporaryDir.resolve(input.nameWithoutExtension + "-expect-platform." + input.extension)
                input.toPath().openZipFileSystem().use { inputFs ->
                    output.toPath().openZipFileSystem(mapOf("create" to true)).use { outputFs ->
                        transformer.transform(inputFs.getPath("/"), outputFs.getPath("/"))
                    }
                }
                from(project.zipTree(output))
            } else if (input.exists()) {
                throw IllegalStateException("ExpectPlatformJar: $input is not a directory or jar file")

            }
        }

        copy()
    }

}
