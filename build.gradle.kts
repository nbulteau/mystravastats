import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://dl.bintray.com/mipt-npm/dataforge")
        maven("https://dl.bintray.com/mipt-npm/kscience")
        maven("https://dl.bintray.com/mipt-npm/dev")
    }
}

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.4.20"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    jcenter()
    mavenCentral()

}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.beust:jcommander:1.78")
    implementation("org.danilopianini:khttp:0.1.0-dev2r+2d3b427")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.0")

    implementation("kscience.plotlykt:plotlykt-server:0.3.0")
    implementation(kotlin("script-runtime"))
    implementation("de.mpicbg.scicomp:krangl:0.13")
    implementation("io.github.microutils:kotlin-logging:2.0.3")


    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
}

application {
    // Define the main class for the application.
    mainClass.set("me.nicolas.stravastats.MyStravaStatsKt")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to "me.nicolas.stravastats.MyStravaStatsKt")
    }

    from(configurations.compileClasspath.map { config -> config.map { if (it.isDirectory) it else zipTree(it) } })
}

