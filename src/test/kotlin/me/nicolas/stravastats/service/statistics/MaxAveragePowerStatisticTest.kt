package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.loadColAgnelActivity
import me.nicolas.stravastats.business.loadZwiftActivity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MaxAveragePowerStatisticTest {

    @Test
    fun `Calculate MaxAveragePowerStatistic with activities without device Watt`() {
        // Given
        val activities = listOf(loadColAgnelActivity())

        // When
        val maxAveragePowerStatistic = MaxAveragePowerStatistic(activities)

        // Then
        assertEquals("Average power", maxAveragePowerStatistic.name)
        assertEquals("164,40 W", maxAveragePowerStatistic.value)
    }

    @Test
    fun `Calculate MaxAveragePowerStatistic with activities with device Watt`() {
        // Given
        val activities = listOf(loadZwiftActivity())

        // When
        val maxAveragePowerStatistic = MaxAveragePowerStatistic(activities)

        // Then
        assertEquals("Average power", maxAveragePowerStatistic.name)
        assertEquals("172 W", maxAveragePowerStatistic.value)
    }
}