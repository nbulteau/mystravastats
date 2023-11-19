package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.GeoCoordinate
import me.nicolas.stravastats.business.loadColAgnelActivity
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FamousClimbBadgeTest {

    @Test
    fun check_ok() {
        // Given
        val colAgnelBadge = FamousClimbBadge(
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
        val colAgnelActivity = loadColAgnelActivity()


        val result = colAgnelBadge.check(listOf(colAgnelActivity))
        assertTrue(result.second)
    }

    @Test
    fun check_ko() {
        // Given
        val colAgnelBadge = FamousClimbBadge(
            label = "Col d'Izoard",
            name = "Col d'Izoard",
            topOfTheAscent = 2362,
            start = GeoCoordinate(44.6839194, 6.9795741),
            end = GeoCoordinate(44.8200267, 6.7350408),
            length = 14.1,
            totalAscent = 1000,
            averageGradient = 7.0,
            difficulty = 810
        )
        val colAgnelActivity = loadColAgnelActivity()


        val result = colAgnelBadge.check(listOf(colAgnelActivity))
        assertFalse(result.second)
    }
}