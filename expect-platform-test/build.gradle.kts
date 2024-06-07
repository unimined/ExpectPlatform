@file:Suppress("DSL_SCOPE_VIOLATION")
import xyz.wagyourtail.unimined.expect.task.ExpectPlatformFiles
import xyz.wagyourtail.unimined.expect.task.ExpectPlatformJar
import xyz.wagyourtail.unimined.expect.ExpectPlatformExtension
import java.util.*

buildscript {
    repositories {
        mavenCentral()
        flatDir {
            dirs("../build/libs")
        }
    }
    dependencies {

        if (!project.hasProperty("runningTest")) {
            classpath("xyz.wagyourtail.unimined.expect-platform:expect-platform:1.0.3")
            classpath("org.ow2.asm:asm:9.7")
            classpath("org.ow2.asm:asm-commons:9.7")
            classpath("org.ow2.asm:asm-tree:9.7")
        }
    }
}

plugins {
    java
    if (project.hasProperty("runningTest")) {
        plugins {
            id("xyz.wagyourtail.unimined.expect-platform")
        }
    }

}


apply(plugin = "xyz.wagyourtail.unimined.expect-platform")


sourceSets {
    create("a") {
        compileClasspath += sourceSets.main.get().output
    }
    create("b") {
        compileClasspath += sourceSets.main.get().output
    }
    create("c") {
        compileClasspath += sourceSets.main.get().output
    }
}

repositories {
    flatDir {
        dirs("../build/libs")
    }
}

val expectPlatform = project.extensions.getByType(ExpectPlatformExtension::class)
expectPlatform.version = run {
    projectDir.parentFile.resolve("gradle.properties").inputStream().use {
        val props = Properties()
        props.load(it)
        props.getProperty("version") as String
    }
}

dependencies {
    implementation(expectPlatform.annotationsDep)
}

val aExpectPlatform by tasks.registering(ExpectPlatformFiles::class) {
    platformName = "a"
    inputCollection = sourceSets.main.get().output

    remap = mapOf(
        "xyz/wagyourtail/unimined/expect/annotation/Environment" to "xyz/wagyourtail/ept/a/Env",
        "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType" to "xyz/wagyourtail/ept/a/Env\$EnvType",
        "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType.COMBINED" to "JOINED",
    )
}

val bExpectPlatform by tasks.registering(ExpectPlatformFiles::class) {
    platformName = "b"
    inputCollection = sourceSets.main.get().output

    remap = mapOf(
            "xyz/wagyourtail/unimined/expect/annotation/Environment" to "xyz/wagyourtail/ept/b/OnlyIn",
            "xyz/wagyourtail/unimined/expect/annotation/Environment.value" to "env",
            "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType" to "xyz/wagyourtail/ept/b/OnlyIn\$Type",
    )
}

val cExpectPlatform by tasks.registering(ExpectPlatformFiles::class) {
    platformName = "c"
    inputCollection = sourceSets.main.get().output

    remap = mapOf(
            "xyz/wagyourtail/unimined/expect/annotation/Environment" to "xyz/wagyourtail/ept/c/Environment",
            "xyz/wagyourtail/unimined/expect/annotation/Environment.value" to "type",
            "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType" to "xyz/wagyourtail/ept/c/Environment\$EnvType",
    )
}

tasks.register("runA", JavaExec::class) {
    dependsOn(aExpectPlatform)
    classpath = sourceSets["a"].runtimeClasspath + aExpectPlatform.get().outputCollection
    mainClass = "xyz.wagyourtail.ept.Main"
    group = "ept"
}

tasks.register("runB", JavaExec::class) {
    dependsOn(bExpectPlatform)
    classpath = sourceSets["b"].runtimeClasspath + bExpectPlatform.get().outputCollection
    mainClass = "xyz.wagyourtail.ept.Main"
    group = "ept"
}

tasks.register("runC", JavaExec::class) {
    dependsOn(cExpectPlatform)
    classpath = sourceSets["c"].runtimeClasspath + cExpectPlatform.get().outputCollection
    mainClass = "xyz.wagyourtail.ept.Main"
    group = "ept"
}

tasks.register("runAgentA", JavaExec::class) {
    classpath = sourceSets["a"].runtimeClasspath + sourceSets.main.get().runtimeClasspath
    mainClass = "xyz.wagyourtail.ept.Main"
    group = "ept"

    expectPlatform.insertAgent(this, "a", mapOf(
                    "xyz/wagyourtail/unimined/expect/annotation/Environment" to "xyz/wagyourtail/ept/a/Env",
                    "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType" to "xyz/wagyourtail/ept/a/Env\$EnvType",
                    "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType.COMBINED" to "JOINED",
    ))
}

tasks.register("runAgentB", JavaExec::class) {
    classpath = sourceSets["b"].runtimeClasspath + sourceSets.main.get().runtimeClasspath
    mainClass = "xyz.wagyourtail.ept.Main"
    group = "ept"

    expectPlatform.insertAgent(this, "b", mapOf(
                    "xyz/wagyourtail/unimined/expect/annotation/Environment" to "xyz/wagyourtail/ept/b/OnlyIn",
                    "xyz/wagyourtail/unimined/expect/annotation/Environment.value" to "env",
                    "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType" to "xyz/wagyourtail/ept/b/OnlyIn\$Type",
            ))
}

tasks.register("runAgentC", JavaExec::class) {
    classpath = sourceSets["c"].runtimeClasspath + sourceSets.main.get().runtimeClasspath
    mainClass = "xyz.wagyourtail.ept.Main"
    group = "ept"

    expectPlatform.insertAgent(this, "c", mapOf(
                    "xyz/wagyourtail/unimined/expect/annotation/Environment" to "xyz/wagyourtail/ept/c/Environment",
                    "xyz/wagyourtail/expect/unimined/annotation/Environment.value" to "type",
                    "xyz/wagyourtail/expect/unimined/annotation/Environment\$EnvType" to "xyz/wagyourtail/ept/c/Environment\$EnvType",
            ))
}

val jarA by tasks.registering(ExpectPlatformJar::class) {
    platformName = "a"
    inputFiles = sourceSets.main.get().output
    from(sourceSets["a"].output)
    archiveFileName = "a.jar"

    remap = mapOf(
            "xyz/wagyourtail/unimined/expect/annotation/Environment" to "xyz/wagyourtail/ept/a/Env",
            "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType" to "xyz/wagyourtail/ept/a/Env\$EnvType",
            "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType.COMBINED" to "JOINED",
    )
}

val jarB by tasks.registering(ExpectPlatformJar::class) {
    platformName = "b"
    inputFiles = sourceSets.main.get().output
    from(sourceSets["b"].output)
    archiveFileName = "b.jar"

    remap = mapOf(
            "xyz/wagyourtail/unimined/expect/annotation/Environment" to "xyz/wagyourtail/ept/b/OnlyIn",
            "xyz/wagyourtail/unimined/expect/annotation/Environment.value" to "env",
            "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType" to "xyz/wagyourtail/ept/b/OnlyIn\$Type",
    )
}

val jarC by tasks.registering(ExpectPlatformJar::class) {
    platformName = "c"
    inputFiles = sourceSets.main.get().output
    from(sourceSets["c"].output)
    archiveFileName = "c.jar"

    remap = mapOf(
            "xyz/wagyourtail/expect/unimined/annotation/Environment" to "xyz/wagyourtail/ept/c/Environment",
            "xyz/wagyourtail/expect/unimined/annotation/Environment.value" to "type",
            "xyz/wagyourtail/expect/unimined/annotation/Environment\$EnvType" to "xyz/wagyourtail/ept/c/Environment\$EnvType",
    )
}

tasks.assemble {
    dependsOn(jarA)
    dependsOn(jarB)
    dependsOn(jarC)

}