@file:Suppress("LeakingThis")

package xyz.wagyourtail.unimined.expect.task

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import xyz.wagyourtail.jvmdg.util.FinalizeOnRead
import xyz.wagyourtail.jvmdg.util.MustSet
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
    fun doDowngrade() {
        for (input in inputFiles) {
            if (input.isDirectory) {
                val output = temporaryDir.resolve(input.name + "-expect-platform")
                TransformPlatform(platformName.get()).transform(input.toPath(), output.toPath())
                from(output)
            } else if (input.extension == "jar") {
                val output = temporaryDir.resolve(input.nameWithoutExtension + "-expect-platform." + input.extension)
                input.toPath().openZipFileSystem().use { inputFs ->
                    output.toPath().openZipFileSystem(mapOf("create" to true)).use { outputFs ->
                        TransformPlatform(platformName.get()).transform(
                            inputFs.getPath("/"),
                            outputFs.getPath("/")
                        )
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
