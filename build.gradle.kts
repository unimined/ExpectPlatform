import java.net.URI

plugins {
    kotlin("jvm") version "1.9.22"
    `java-gradle-plugin`
    `maven-publish`
}

group = "xyz.wagyourtail.unimined.expect-platform"
version = if (project.hasProperty("version_snapshot")) project.properties["version"] as String + "-SNAPSHOT" else project.properties["version"] as String

base {
    archivesName.set("expect-platform")
}

val annotations by sourceSets.creating {}

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

dependencies {
    implementation(gradleApi())
    implementation("org.ow2.asm:asm-tree:${asmVersion}")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {

    manifest {
        attributes(
            "Manifest-Version" to "1.0",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }
}

val annotationJar = tasks.register<Jar>("annotationJar") {
    archiveClassifier.set("annotations")
    from(annotations.output)

    manifest {
        attributes(
            "Manifest-Version" to "1.0",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }
}

tasks.assemble {
    dependsOn(annotationJar)
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
        create<MavenPublication>("mavenJava") {
            groupId = "xyz.wagyourtail.unimined.expect-platform"
            artifactId = "expect-platform"
            version = project.version.toString()

            from(components["java"])

            artifact(annotationJar) {
                classifier = "annotations"
            }
        }
    }
}
