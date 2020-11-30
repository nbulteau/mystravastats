import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
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
    implementation("org.danilopianini:khttp:0.1.0-dev1u+b23a8f7")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.3")

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
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to "me.nicolas.stravastats.MyStravaStatsKt")
    }

    from(configurations.compileClasspath.map { config -> config.map { if (it.isDirectory) it else zipTree(it) } })
}

