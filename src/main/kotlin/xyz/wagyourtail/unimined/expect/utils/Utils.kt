package xyz.wagyourtail.unimined.expect.utils

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists
import kotlin.io.path.outputStream

fun Path.openZipFileSystem(args: Map<String, *> = mapOf<String, Any>()): FileSystem {
    if (!exists() && args["create"] == true) {
        ZipOutputStream(outputStream()).use { stream ->
            stream.closeEntry()
        }
    }

    return FileSystems.newFileSystem(URI.create("jar:${toUri()}"), args, null)
}

fun ClassNode.toByteArray(): ByteArray {
    val writer = ClassWriter(0)
    accept(writer)
    return writer.toByteArray()
}