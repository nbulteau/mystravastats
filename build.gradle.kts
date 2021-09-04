import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.30"
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("com.github.ben-manes.versions") version "0.39.0"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://repo.kotlin.link")
}


dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.danilopianini:khttp:0.1.0-dev30+51fa9ae")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.5")

    implementation("io.javalin:javalin:3.13.11")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.slf4j:slf4j-nop:1.7.32")

    // Some problem with 0.5.0 version
    implementation("space.kscience:plotlykt-server:0.4.4") {
        exclude("ch.qos.logback", "logback-classic")
    }

    implementation("no.tornado:tornadofx:1.7.20")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
}

javafx {
    modules = listOf("javafx.controls", "javafx.media", "javafx.fxml", "javafx.web", "javafx.graphics")
}

application {
    // Define the main class for the application.
    mainClass.set("me.nicolas.stravastats.MyStravaStatsAppKt")
    applicationDefaultJvmArgs = listOf(
        "-Xmx2048m",
        "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.net=ALL-UNNAMED",
        "--add-opens=java.base/sun.net=ALL-UNNAMED",
        "--add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED",
        "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED"
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
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

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        jvmTarget = "16"
        moduleName = "mystravastats"
    }
}

