package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.loadColAgnelActivity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BestElevationDistanceStatisticKtTest {

    @Test
    fun calculateBestElevationForDistance() {

        // Given
        val activities = listOf(loadColAgnelActivity())

        // When
        val bestElevationDistanceStatistic = BestElevationDistanceStatistic("Max gradient for 1000 m", activities, 1000.0)

        // Then
        assertEquals("Max gradient for 1000 m", bestElevationDistanceStatistic.name)
        assertEquals("9,10 %", bestElevationDistanceStatistic.value)
    }
}