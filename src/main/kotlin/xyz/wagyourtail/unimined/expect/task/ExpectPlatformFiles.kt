package xyz.wagyourtail.unimined.expect.task

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import xyz.wagyourtail.unimined.expect.utils.FinalizeOnRead
import xyz.wagyourtail.unimined.expect.utils.MustSet
import xyz.wagyourtail.unimined.expect.TransformPlatform
import xyz.wagyourtail.unimined.expect.expectPlatform
import xyz.wagyourtail.unimined.expect.transform.ExpectPlatformParams
import xyz.wagyourtail.unimined.expect.utils.openZipFileSystem
import java.io.File
import java.nio.file.FileSystem
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name

abstract class ExpectPlatformFiles : ConventionTask(), ExpectPlatformParams {

    @get:InputFiles
    var inputCollection: FileCollection by FinalizeOnRead(MustSet())

    @get:Internal
    val outputMap: Map<File, File>
        get() = inputCollection.associateWith { temporaryDir.resolve(it.name) }

    /**
     * this is the true output, gradle just doesn't have a
     * \@OutputDirectoriesAndFiles
     */
    @get:Internal
    val outputCollection: FileCollection by lazy {
        val fd = inputCollection.map { it to temporaryDir.resolve(it.name) }

        outputs.dirs(*fd.filter { it.first.isDirectory }.map { it.second }.toTypedArray())
        outputs.files(*fd.filter { it.first.isFile }.map { it.second }.toTypedArray())

        outputs.files
    }

    init {
        stripAnnotations.convention(project.expectPlatform.stripAnnotations)
    }

    @TaskAction
    fun doTransform() {
        var toTransform = inputCollection.map { it.toPath() }.filter { it.exists() }

        val fileSystems = mutableSetOf<FileSystem>()

        try {
            outputs.files.forEach { it.deleteRecursively() }

            val transformed = toTransform.map { temporaryDir.resolve(it.name) }.map {
                if (it.extension == "jar" || it.extension == "zip") {
                    val fs = it.toPath().openZipFileSystem(mapOf("create" to true))
                    fileSystems.add(fs)
                    fs.getPath("/")
                } else it.toPath()
            }

            toTransform = toTransform.map {
                if (it.isDirectory()) it else run {
                    val fs = it.openZipFileSystem()
                    fileSystems.add(fs)
                    fs.getPath("/")
                }
            }

            val transformer = TransformPlatform(platformName.get(), remap.get(), stripAnnotations.get())

            for (i in toTransform.indices) {
                val input = toTransform[i]
                val output = transformed[i]
                transformer.transform(input, output)
            }
        } finally {
            fileSystems.forEach { it.close() }
        }
    }

}