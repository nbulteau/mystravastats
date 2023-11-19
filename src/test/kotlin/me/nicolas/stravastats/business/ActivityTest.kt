package me.nicolas.stravastats.business

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ActivityTest {

    @Test
    fun getSpeed() {

        // Given
        val colAgnelActivity = loadColAgnelActivity()

        // When
        val result = colAgnelActivity.getSpeed()

        // Then
        assertEquals("15,48", result)
    }

    @Test
    fun getTotalElevationGain() {

        // Given
        val colAgnelActivity = loadColAgnelActivity()

        // When
        val result = colAgnelActivity.totalElevationGain

        // Then
        assertEquals(2090, result.toInt())
    }

    @Test
    fun getTotalAscentGain() {

        // Given
        val colAgnelActivity = loadColAgnelActivity()

        // When
        val result = colAgnelActivity.calculateTotalAscentGain()

        // Then
        assertEquals(2107, result.toInt())
    }

    @Test
    fun getTotalDescentGain() {

        // Given
        val colAgnelActivity = loadColAgnelActivity()

        // When
        val result = colAgnelActivity.calculateTotalDescentGain()

        // Then
        assertEquals(2131, result.toInt())
    }
}