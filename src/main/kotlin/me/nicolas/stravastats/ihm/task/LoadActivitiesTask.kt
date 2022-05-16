package me.nicolas.stravastats.ihm.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import javafx.concurrent.Task
import me.nicolas.stravastats.MyStravaStatsProperties
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete


internal interface LoadActivitiesTaskCompletionHandler {
    fun complete()
}

internal abstract class LoadActivitiesTask(open val clientId: String) : Task<Pair<Athlete?, List<Activity>>>() {

    /**
     * Load properties from application.yml
     */
    protected fun loadPropertiesFromFile(): MyStravaStatsProperties {
        val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
        mapper.registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        ) // Enable Kotlin support

        val inputStream = javaClass.getResourceAsStream("/application.yml")

        return mapper.readValue(inputStream, MyStravaStatsProperties::class.java)
    }
}

