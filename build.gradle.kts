import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*


buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("com.github.ben-manes.versions") version "0.45.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2")

    implementation("org.slf4j:slf4j-nop:2.0.6")
    implementation("io.javalin:javalin:5.3.2")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation("com.sothawo:mapjfx:3.1.0")
    implementation("no.tornado:tornadofx:1.7.20")

    // Some trouble with javafx dependencies
    //implementation("org.openjfx:javafx-base:18.0.1")
    //implementation("org.openjfx:javafx-controls:18.0.1")
    implementation("org.openjfx:javafx-fxml:18.0.1")
    //implementation("org.openjfx:javafx-graphics:18.0.1")
    implementation("org.openjfx:javafx-media:18.0.1")
    implementation("org.openjfx:javafx-web:18.0.1")

    // Some problem with 0.5.0 version
    implementation("space.kscience:plotlykt-server:0.5.0") {
        exclude("ch.qos.logback", "logback-classic")
    }

    implementation(files("libs/fit.jar"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

javafx {
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
        "--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED"
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType(Jar::class) {
    manifest {
        attributes["Manifest-Version"] = "1.0"
        attributes["Main-Class"] = "me.nicolas.stravastats.MyStravaStatsApp"
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase(Locale.getDefault()).contains(it) }
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
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        jvmTarget = "18"
        moduleName = "mystravastats"
    }
}

tasks.withType<JavaCompile> {
    targetCompatibility = "18"
}

