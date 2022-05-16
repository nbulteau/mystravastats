package me.nicolas.stravastats.business.badges

import com.fasterxml.jackson.databind.ObjectMapper
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.GeoCoordinate
import me.nicolas.stravastats.business.Stream
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.File

internal class FamousClimbBadgeTest {

    @Test
    fun check() {
        // Given
        val famousClimbBadge = FamousClimbBadge(
            label = "Col_Agnel",
            name = "Col Agnel",
            topOfTheAscent = 2500,
            start = GeoCoordinate(44.6839194, 6.9795741),
            end = GeoCoordinate(44.76234, 6.820959),
            length = 20.7,
            totalAscent = 1364,
            averageGradient = 6.6,
            difficulty = 1030
        )
        val colAgnel = loadColAgnelActivity()


        val result = famousClimbBadge.check(listOf(colAgnel))
        assertTrue(result.second)
    }

    private fun loadColAgnelActivity(): Activity {
        val objectMapper = ObjectMapper()
        var url = Thread.currentThread().contextClassLoader.getResource("colagnel-activity.json")
        var jsonFile = File(url.path)
        val activity =  objectMapper.readValue(jsonFile, Activity::class.java)

        url = Thread.currentThread().contextClassLoader.getResource("colagnel-stream.json")
        jsonFile = File(url.path)
        activity.stream = objectMapper.readValue(jsonFile, Stream::class.java)

        return activity
    }
}