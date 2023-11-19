package me.nicolas.stravastats.business

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ActivityEffortTest {

    @Test
    fun getSpeed() {

        // Given
        val colAgnelActivity = loadColAgnelActivity()

        // When
        val colAgnelActivityEffort = ActivityEffort(colAgnelActivity, colAgnelActivity.distance, colAgnelActivity.elapsedTime, colAgnelActivity.totalElevationGain, 0, 10)

        // Then
        assertEquals("15,48 km/h", colAgnelActivityEffort.getFormattedSpeed())
        assertEquals("15,48", colAgnelActivityEffort.getSpeed())

        assertEquals("2,33 %", colAgnelActivityEffort.getFormattedGradient())
        assertEquals("2,33", colAgnelActivityEffort.getGradient())
    }
}