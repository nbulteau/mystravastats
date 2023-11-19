package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.loadColAgnelActivity
import me.nicolas.stravastats.business.loadZwiftActivity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MaxWeightedAveragePowerStatisticTest {

    @Test
    fun `Calculate MaxWeightedAveragePowerStatistic with activities without device Watt`() {
        // Given
        val activities = listOf(loadColAgnelActivity())

        // When
        val maxWeightedAveragePowerStatistic = MaxWeightedAveragePowerStatistic(activities)

        // Then
        assertEquals("Weighted average power", maxWeightedAveragePowerStatistic.name)
        assertEquals("0 W", maxWeightedAveragePowerStatistic.value)
    }

    @Test
    fun `Calculate MaxWeightedAveragePowerStatistic with activities with device Watt`() {
        // Given
        val activities = listOf(loadZwiftActivity())

        // When
        val maxWeightedAveragePowerStatistic = MaxWeightedAveragePowerStatistic(activities)

        // Then
        assertEquals("Weighted average power", maxWeightedAveragePowerStatistic.name)
        assertEquals("172 W", maxWeightedAveragePowerStatistic.value)
    }
}