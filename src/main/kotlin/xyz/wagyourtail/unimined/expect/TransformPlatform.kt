package xyz.wagyourtail.unimined.expect

import org.objectweb.asm.*
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.math.max

object TransformPlatform {

    @OptIn(ExperimentalPathApi::class)
    fun expectPlatform(inputRoot: Path, outputRoot: Path, platformName: String) {
        for (path in inputRoot.walk()) {
            if (path.isDirectory()) {
                outputRoot.resolve(inputRoot.relativize(path).toString()).createDirectories()
            } else {
                if (path.extension == "class") {
                    val output = outputRoot.resolve(inputRoot.relativize(path).toString())
                    val reader = ClassReader(path.readBytes())
                    val writer = ClassWriter(reader, 0)
                    lateinit var className: String

                    reader.accept(object : ClassVisitor(Opcodes.ASM9, writer) {

                        override fun visit(
                            version: Int,
                            access: Int,
                            name: String,
                            signature: String?,
                            superName: String?,
                            interfaces: Array<out String>?
                        ) {
                            super.visit(version, access, name, signature, superName, interfaces)
                            className = name
                        }

                        override fun visitMethod(
                            access: Int,
                            name: String,
                            descriptor: String,
                            signature: String?,
                            exceptions: Array<out String>?
                        ): MethodVisitor {
                            var hasExpectPlatforms: Boolean = false
                            var platformOverride: String? = null
                            val superDelegate = super.visitMethod(access, name, descriptor, signature, exceptions)

                            return object : MethodVisitor(api, superDelegate) {
                                override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
                                    if (descriptor == "Lxyz/wagyourtail/unimined/expect/annotation/ExpectPlatform;") {
                                        hasExpectPlatforms = true
                                        return object : AnnotationVisitor(api, super.visitAnnotation(descriptor, visible)) {
                                            override fun visitArray(name: String?): AnnotationVisitor {
                                                if (name == "platforms") {
                                                    return object : AnnotationVisitor(api, super.visitArray(name)) {
                                                        override fun visitAnnotation(
                                                            name: String?,
                                                            descriptor: String?
                                                        ): AnnotationVisitor {
                                                            return object : AnnotationVisitor(api, super.visitAnnotation(name, descriptor)) {
                                                                var isPlatform = false
                                                                var target: String? = null

                                                                override fun visit(name: String?, value: Any?) {
                                                                    if (name == "name") {
                                                                        if (value == platformName) {
                                                                            isPlatform = true
                                                                        }
                                                                    } else if (name == "target") {
                                                                        target = value as String
                                                                    }
                                                                    if (isPlatform && target != null) {
                                                                        platformOverride = target
                                                                    }
                                                                    super.visit(name, value)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                return super.visitArray(name)
                                            }
                                        }
                                    }
                                    return super.visitAnnotation(descriptor, visible)
                                }

                                override fun visitCode() {
                                    super.visitCode()
                                    if (hasExpectPlatforms) {
                                        mv = null
                                    }
                                }

                                override fun visitEnd() {
                                    if (hasExpectPlatforms) {
                                        if (access and Opcodes.ACC_STATIC == 0 || access and Opcodes.ACC_PUBLIC == 0) {
                                            throw IllegalStateException("ExpectPlatform can only be applied to public static methods, found ${className};${name};${descriptor}")
                                        }
                                        val platformClassName = platformOverride ?: "${className.substringBeforeLast("/")}/${platformName}/${className.substringAfterLast("/")}Impl"

                                        superDelegate.visitCode()
                                        var i = 0
                                        for (arg in Type.getArgumentTypes(descriptor)) {
                                            superDelegate.visitVarInsn(arg.getOpcode(Opcodes.ILOAD), i)
                                            i += arg.size
                                        }
                                        superDelegate.visitMethodInsn(Opcodes.INVOKESTATIC, platformClassName, name, descriptor, false)
                                        val returnType = Type.getReturnType(descriptor)
                                        superDelegate.visitInsn(returnType.getOpcode(Opcodes.IRETURN))
                                        superDelegate.visitMaxs(max(i, returnType.size), i)
                                        superDelegate.visitEnd()
                                    }
                                }

                            }
                        }
                    }, 0)
                    output.createParentDirectories()
                    output.writeBytes(writer.toByteArray())
                } else {
                    outputRoot.resolve(inputRoot.relativize(path).toString()).writeBytes(path.readBytes())
                }
            }
        }
    }



}