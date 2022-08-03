package me.nicolas.stravastats.business.badges

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DistanceBadgeTest {

    @Test
    fun check_ok() {

        val tenKilometersBadge = DistanceBadge(
            label = "Hit the road 10 km",
            distance = 10000
        )

        val colAgnelActivity = loadColAgnelActivity()

        val result = tenKilometersBadge.check(listOf(colAgnelActivity))
        assertTrue(result.second)
    }

    @Test
    fun check_ko() {

        val hundredKilometersBadge = DistanceBadge(
            label = "Hit the road 100 km",
            distance = 100000
        )

        val colAgnelActivity = loadColAgnelActivity()

        val result = hundredKilometersBadge.check(listOf(colAgnelActivity))
        assertFalse(result.second)
    }
}