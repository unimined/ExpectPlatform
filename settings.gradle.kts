pluginManagement.repositories {
    gradlePluginPortal()
    maven("https://maven.wagyourtail.xyz/snapshots")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "expect-platform"

