import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask


buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    id("com.github.ben-manes.versions") version "0.38.0"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/mipt-npm/dataforge")
    maven("https://dl.bintray.com/mipt-npm/kscience")
    maven("https://dl.bintray.com/mipt-npm/dev")
}


dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.beust:jcommander:1.81")
    implementation("org.danilopianini:khttp:0.1.0-dev30+51fa9ae")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.3")

    implementation("io.javalin:javalin:3.13.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("org.slf4j:slf4j-nop:1.7.30")

    implementation("kscience.plotlykt:plotlykt-server:0.3.1") {
        exclude("ch.qos.logback", "logback-classic")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
}

application {
    // Define the main class for the application.
    mainClass.set("me.nicolas.stravastats.MyStravaStatsKt")
    applicationDefaultJvmArgs = listOf("-Xmx2048m")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes("Main-Class" to "me.nicolas.stravastats.MyStravaStatsKt")
    }

    from(configurations.compileClasspath.map { config ->
        config.map { file ->
            if (file.isDirectory) file else zipTree(file)
        }
    })
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

