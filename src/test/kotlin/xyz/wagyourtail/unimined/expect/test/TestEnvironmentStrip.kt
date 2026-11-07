package xyz.wagyourtail.unimined.expect.test

import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import org.junit.jupiter.api.Test
import xyz.wagyourtail.unimined.expect.TransformPlatform
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestEnvironmentStrip {

    private val ENV = "Lxyz/wagyourtail/unimined/expect/annotation/Environment;"
    private val ENV_TYPE = "Lxyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType;"

    private fun envAnnotation(value: String) = AnnotationNode(ENV).apply {
        this.values = mutableListOf<Any>("value", arrayOf(ENV_TYPE, value))
    }

    private fun method(name: String, side: String? = null) =
        MethodNode().apply {
            this.name = name
            desc = "()V"
            if (side != null) visibleAnnotations = listOf(envAnnotation(side))
        }

    private fun node(withTypeSide: String? = null): ClassNode {
        val node = ClassNode()
        node.name = "xyz/Test"
        if (withTypeSide != null) {
            node.visibleAnnotations = listOf(envAnnotation(withTypeSide))
        }
        node.methods = mutableListOf(
            method("keepMe"),
            method("clientOnly", "CLIENT"),
            method("serverOnly", "SERVER"),
            method("combined", "COMBINED"),
        )
        node.fields = mutableListOf(
            FieldNode(0, "keepField", "I", null, null),
            FieldNode(0, "serverField", "I", null, null).apply {
                visibleAnnotations = listOf(envAnnotation("SERVER"))
            },
        )
        return node
    }

    @Test
    fun `client side strips server-annotated members`() {
        val out = TransformPlatform("fabric", "", false, "client")
            .transform(node())
        assertEquals(setOf("keepMe", "clientOnly", "combined"),
            out.methods.map { it.name }.toSet())
        assertEquals(listOf("keepField"), out.fields.map { it.name })
    }

    @Test
    fun `wrong-side type is emptied`() {
        val out = TransformPlatform("fabric", "", false, "server")
            .transform(node("CLIENT"))
        assertTrue(out.methods.isEmpty())
        assertTrue(out.fields.isEmpty())
    }

    @Test
    fun `no environment means no stripping`() {
        val out = TransformPlatform("fabric", "", false)
            .transform(node())
        assertEquals(4, out.methods.size)
        assertEquals(2, out.fields.size)
    }

    @Test
    fun `combined members survive both sides`() {
        val out = TransformPlatform("fabric", "", false, "server")
            .transform(node())
        assertTrue(out.methods.any { it.name == "combined" })
    }

}
