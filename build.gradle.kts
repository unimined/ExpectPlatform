@file:Suppress("HasPlatformType")

import xyz.wagyourtail.commons.gradle.shadow.ShadowJar
import java.net.URI

plugins {
    kotlin("jvm") version "1.9.22"
    id("xyz.wagyourtail.commons-gradle") version "1.0.4-SNAPSHOT"
    `java-gradle-plugin`
    `maven-publish`
}

group = "xyz.wagyourtail.unimined.expect-platform"
version = if (project.hasProperty("version_snapshot")) project.properties["version"] as String + "-SNAPSHOT" else project.properties["version"] as String

base {
    archivesName.set("expect-platform")
}

val annotations by sourceSets.creating

val shared by sourceSets.creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

val agent by sourceSets.creating {
    compileClasspath += shared.output + sourceSets.main.get().compileClasspath
    runtimeClasspath += shared.output + sourceSets.main.get().runtimeClasspath
}

sourceSets.main {
    compileClasspath += shared.output
    runtimeClasspath += shared.output
}

sourceSets.test {
    compileClasspath += shared.output
    runtimeClasspath += shared.output
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
}

val asmVersion: String by project.properties

val shade by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    implementation(gradleApi())

    shade("org.ow2.asm:asm:${asmVersion}")
    shade("org.ow2.asm:asm-commons:${asmVersion}")
    shade("org.ow2.asm:asm-tree:${asmVersion}")

    testImplementation(kotlin("test"))
}

tasks.jar {
    from(shared.output)

    manifest {
        attributes(
            "Manifest-Version" to "1.0",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }
}

val annotationJar = tasks.register<Jar>("annotationJar") {
    archiveBaseName.set("expect-platform-annotations")
    from(annotations.output)

    manifest {
        attributes(
            "Manifest-Version" to "1.0",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }
}

val annotationSources = tasks.register<Jar>("annotationSources") {
    archiveBaseName.set("expect-platform-annotations")
    archiveClassifier.set("sources")
    from(annotations.allSource)
}

val agentShadeJar = tasks.register<ShadowJar>("agentShadowJar") {
    archiveBaseName.set("expect-platform-agent")

    from(agent.output, shared.output)

    shadowContents.add(shade)
    exclude("module-info.class")

    relocate("org.objectweb.asm", "xyz.wagyourtail.unimined.expect.asm")

    manifest {
        attributes(
            "Manifest-Version" to "1.0",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Premain-Class" to "xyz.wagyourtail.unimined.expect.ExpectPlatformAgent",
            "Can-Redefine-Classes" to "true",
        )
    }
}

tasks.test {
    useJUnitPlatform()
    dependsOn(tasks.jar, annotationJar, agentShadeJar)
}

tasks.assemble {
    dependsOn(annotationJar)
    dependsOn(agentShadeJar)
}

kotlin {
    jvmToolchain(8)
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "xyz.wagyourtail.unimined.expect-platform"
            implementationClass = "xyz.wagyourtail.unimined.expect.ExpectPlatformPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "WagYourMaven"
            url = if (project.hasProperty("version_snapshot")) {
                URI.create("https://maven.wagyourtail.xyz/snapshots/")
            } else {
                URI.create("https://maven.wagyourtail.xyz/releases/")
            }
            credentials {
                username = project.findProperty("mvn.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("mvn.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("agent") {
            groupId = "xyz.wagyourtail.unimined.expect-platform"
            artifactId = "expect-platform-agent"
            version = project.version.toString()

            artifact(agentShadeJar)
        }

        create<MavenPublication>("annotations") {
            groupId = "xyz.wagyourtail.unimined.expect-platform"
            artifactId = "expect-platform-annotations"
            version = project.version.toString()

            artifact(annotationJar)
            artifact(annotationSources)
        }
    }
}
