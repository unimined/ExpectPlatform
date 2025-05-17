# ExpectPlatform

This is a simple project that implements `@ExpectPlatform` and `@PlatformOnly` in a non-specific way.

## Usage

to use this project, you can add the following to your `settings.gradle` file:
```gradle
pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = "https://maven.wagyourtail.xyz/releases"
        }
        maven {
            url = "https://maven.wagyourtail.xyz/snapshots"
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
```

and then add the plugin to your `build.gradle` file:
```gradle
plugins {
    id 'xyz.wagyourtail.unimined.expect-platform' version '1.0.5'
}
```

you can add the annotations to be accessible with
```gradle
dependencies {
    implementation(expectPlatform.annotationsDep)
    // or
    implementation("xyz.wagyourtail.unimined:expect-platform-annotations:${expectPlatform.version}")
}
```

to then apply the annotations in a subproject or other module, you can either do:

```gradle
tasks.create("expectPlatformOutput", xyz.wagyourtail.unimined.expect.ExpectPlatformFiles) {
    group = "unimined"
    platformName = platformName
    inputCollection = rootProject.sourceSets["main"].output
}

dependencies {
    common(expectPlatformOutput.outputCollection)
}
```

or, you can apply the agent to a JavaExec and create a task to make the final jar:

```gradle
tasks.withType(JavaExec) {
    expectPlatform.insertAgent(it, platformName)
}

tasks.create("PlatformJar", xyz.wagyourtail.unimined.expect.ExpectPlatformJar) {
    group = "unimined"
    dependsOn("jar")
   
    inputFiles = tasks.jar.outputs.files
    platformName = platformName
}

```

if you need the agent on a runner that isn't implementing JavaExecSpec (i.e. the one in unimined 1.2), call
`expectPlatform.getAgentArgs(platformName)` to get the args to pass to the jvm.

### Environment

This plugin also provides an `@Environment` annotation, but doesn't hook it up.
to do so, you can use the following code snippets (as either setting in the task, or the third arg to the agent):
```gradle
// neoforge
    remap = [
        "xyz/wagyourtail/unimined/expect/annotation/Environment": "net/neoforged/api/distmarker/OnlyIn",
        "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType": "net/neoforged/api/distmarker/Dist",
        "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType.SERVER": "DEDICATED_SERVER",
    ]
// fabric
    remap = [
        "xyz/wagyourtail/unimined/expect/annotation/Environment": "net/fabricmc/api/Environment",
        "xyz/wagyourtail/unimined/expect/annotation/Environment\$EnvType": "net/fabricmc/api/EnvType",
    ]
```

Do note that while this plugin contains `EnvType.COMBINED`, it is not present in fabric or neoforge, so it is not recommended to use it.