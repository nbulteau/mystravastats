package me.nicolas.stravastats

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.nicolas.stravastats.business.Activity
import java.io.File

class TestHelper {
    companion object {
        fun loadActivities(): List<Activity> {
            val url = Thread.currentThread().contextClassLoader.getResource("activities.json")
            val jsonFile = File(url.path)
            return jacksonObjectMapper().readValue(jsonFile, Array<Activity>::class.java).toList()
        }
    }
}