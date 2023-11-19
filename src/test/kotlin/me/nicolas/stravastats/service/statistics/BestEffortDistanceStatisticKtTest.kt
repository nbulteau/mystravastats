package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.loadColAgnelActivity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BestEffortDistanceStatisticKtTest {

    @Test
    fun calculateBestTimeForDistance() {

        // Given
        val activities = listOf(
            loadColAgnelActivity()
        )

        // When
        val bestEffortDistanceStatistic = BestEffortDistanceStatistic("Best 1000 m", activities, 1000.0)

        // Then
        assertEquals("Best 1000 m", bestEffortDistanceStatistic.name)
        assertEquals("01m 12s => 50,00 km/h", bestEffortDistanceStatistic.value)
    }
}