package xyz.wagyourtail.unimined.expect.test

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.inputStream

class TestExpectPlatform {
    val version = run {
        Paths.get("./gradle.properties").inputStream().use {
            val props = Properties()
            props.load(it)
            props.getProperty("asmVersion") as String
        }
    }
    val classpath = System.getProperty("java.class.path")

    fun constructGradle() = GradleRunner.create()
        .withProjectDir(File("expect-platform-test"))
        .withPluginClasspath()
        .withPluginClasspath(classpath.split(File.pathSeparator).map { File(it) })

    @Test
    fun build() {

        val result = constructGradle()
            .withArguments("clean", "build", "--stacktrace", "-PrunningTest")
            .build()

        assert(result.output.contains("BUILD SUCCESSFUL"))

    }

    @Test
    fun runA() {
        val result = constructGradle()
            .withArguments("runA", "--stacktrace", "-PrunningTest")
            .build()

        assert(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun runB() {
        val result = constructGradle()
            .withArguments("runB", "--stacktrace", "-PrunningTest")
            .build()

        assert(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun runC() {
        val result = constructGradle()
            .withArguments("runC", "--stacktrace", "-PrunningTest")
            .build()

        assert(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun runAgentA() {
        val result = constructGradle()
            .withArguments("runAgentA", "--stacktrace", "-PrunningTest")
            .build()

        assert(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun runAgentB() {
        val result = constructGradle()
            .withArguments("runAgentB", "--stacktrace", "-PrunningTest")
            .build()

        assert(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun runAgentC() {
        val result = constructGradle()
            .withArguments("runAgentC", "--stacktrace", "-PrunningTest")
            .build()

        assert(result.output.contains("BUILD SUCCESSFUL"))
    }

}
