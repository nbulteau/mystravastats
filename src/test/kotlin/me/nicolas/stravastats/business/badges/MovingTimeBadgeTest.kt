package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.loadColAgnelActivity
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MovingTimeBadgeTest {

    @Test
    fun check_ok() {

        val fiveHourBadge = MovingTimeBadge(
            label = "Moving time 5 hours",
            movingTime = 18000
        )
        val colAgnelActivity = loadColAgnelActivity()
        val result = fiveHourBadge.check(listOf(colAgnelActivity))
        assertTrue(result.second)
    }

    @Test
    fun check_ko() {

        val sixHourBadge = MovingTimeBadge(
            label = "Moving time 6 hours",
            movingTime = 21600
        )
        val colAgnelActivity = loadColAgnelActivity()
        val result = sixHourBadge.check(listOf(colAgnelActivity))
        assertFalse(result.second)
    }
}