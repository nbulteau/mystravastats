package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.loadColAgnelActivity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BestEffortTimeStatisticKtTest {

    @Test
    fun calculateBestDistanceForTime() {

        // Given
        val activities = listOf(loadColAgnelActivity())

        // When
        val bestEffortTimeStatistic = BestEffortTimeStatistic("Best 1 h", activities, 60 * 60)

        // Then
        assertEquals("Best 1 h", bestEffortTimeStatistic.name)
        assertEquals("33,49 km => 33,49 km/h", bestEffortTimeStatistic.value)
    }
}