import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.util.*

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.9.0"
    id("org.openjfx.javafxplugin") version "0.0.14"
    id("com.github.ben-manes.versions") version "0.47.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")

    implementation("org.slf4j:slf4j-nop:2.0.7")
    implementation("io.javalin:javalin:5.6.2")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    implementation("com.sothawo:mapjfx:3.1.0")
    implementation("no.tornado:tornadofx:1.7.20")

    implementation("space.kscience:plotlykt-server:0.5.3")

    implementation(files("libs/fit.jar"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(18)
}

javafx {
    version = "18"
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
        jvmTarget = "18"
        moduleName = "mystravastats"
    }
}

tasks.dependencyUpdates {
    // reject all non stable versions
    rejectVersionIf {
        isNonStable(candidate.version)
    }

    // disallow release candidates as upgradable versions from stable versions
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }

    // using the full syntax
    resolutionStrategy {
        componentSelection {
            all {
                if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                    reject("Release candidate")
                }
            }
        }
    }
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    // optional parameters
    checkForGradleUpdate = true
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase(Locale.getDefault()).contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}