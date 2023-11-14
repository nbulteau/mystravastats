buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.9.20"
    id("org.graalvm.buildtools.native") version "0.9.28"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.ben-manes.versions") version "0.49.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.3")

    implementation("org.slf4j:slf4j-nop:2.0.9")
    implementation("io.javalin:javalin:5.6.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.sothawo:mapjfx:3.1.0")
    implementation("no.tornado:tornadofx:1.7.20")

    implementation("space.kscience:plotlykt-server:0.5.3")

    implementation(files("libs/fit.jar"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.media", "javafx.fxml", "javafx.web", "javafx.graphics")
}

application {
    // Define the main class for the application.
    mainClass.set("me.nicolas.stravastats.MyStravaStatsAppKt")
    applicationDefaultJvmArgs = listOf(
        "-Xmx2048m",
        "-Dprism.maxvram=2G",
        "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.net=ALL-UNNAMED",
        "--add-opens=java.base/sun.net=ALL-UNNAMED",
        "--add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED",
        "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED",
    )
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Manifest-Version"] = "1.0"
        attributes["Main-Class"] = "me.nicolas.stravastats.MyStravaStatsApp"
    }
}

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        jvmTarget = "21"
        moduleName = "mystravastats"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    jvmTargetValidationMode.set(org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode.WARNING)
}

graalvmNative {
    binaries {

        named("main") {
            fallback.set(false)
            verbose.set(true)

            buildArgs.add("--initialize-at-build-time=ch.qos.logback")
            buildArgs.add("--initialize-at-build-time=io.ktor,kotlin")
            buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")

            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")

            imageName.set("58H-server")
        }

        named("test") {
            fallback.set(false)
            verbose.set(true)

            buildArgs.add("--initialize-at-build-time=ch.qos.logback")
            buildArgs.add("--initialize-at-build-time=io.ktor,kotlin")
            buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")

            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")

            val path = "${projectDir}/src/test/resources/META-INF/native-image/"
            buildArgs.add("-H:ReflectionConfigurationFiles=${path}reflect-config.json")
            buildArgs.add("-H:ResourceConfigurationFiles=${path}resource-config.json")

            imageName.set("58H-test-server")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}