package me.nicolas.stravastats

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.net.URL
import java.nio.file.Files

internal class PropertiesHelper {

    fun loadPropertiesFromFile(fileName: String): StravaStatsProperties {

        val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
        mapper.registerModule(KotlinModule()) // Enable Kotlin support

        val file = getFileFromResource(fileName)

        return Files.newBufferedReader(file?.toPath()).use {
            mapper.readValue(it, StravaStatsProperties::class.java)
        }
    }

    private fun getFileFromResource(fileName: String): File? {

        val classLoader: ClassLoader = javaClass.classLoader
        val resource: URL? = classLoader.getResource(fileName)
        return if (resource == null) {
            throw IllegalArgumentException("file not found! $fileName")
        } else {
            // failed if files have whitespaces or special characters
            File(resource.toURI())
        }
    }
}