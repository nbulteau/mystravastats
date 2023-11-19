package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.loadColAgnelActivity
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ElevationBadgeTest {

    @Test
    fun check_ok() {

        val twoThousandElevationBadge = ElevationBadge(
            label = "Ride that climb",
            totalElevationGain = 2000
        )

        val colAgnelActivity = loadColAgnelActivity()

        val result = twoThousandElevationBadge.check(listOf(colAgnelActivity))
        assertTrue(result.second)
    }

    @Test
    fun check_ko() {

        val threeThousandElevationBadge = ElevationBadge(
            label = "Ride that climb",
            totalElevationGain = 3000
        )

        val colAgnelActivity = loadColAgnelActivity()

        val result = threeThousandElevationBadge.check(listOf(colAgnelActivity))
        assertFalse(result.second)
    }
}