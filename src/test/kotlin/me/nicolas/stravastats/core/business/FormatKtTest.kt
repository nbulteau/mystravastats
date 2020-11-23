package me.nicolas.stravastats.core.business

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FormatKtTest {

    @Test
    fun formatSeconds() {

        assertEquals("00:00", 0.formatSeconds())
        assertEquals("00:59", 59.formatSeconds())
        assertEquals("01:00", 60.formatSeconds())
        assertEquals("02:00", 120.formatSeconds())
        assertEquals("59:59", 3599.formatSeconds())
    }

    @Test
    fun formatHundredths() {

        assertEquals("0:00.00", 0.0.formatHundredths())
        assertEquals("0:59.00", 59.0.formatHundredths())
        assertEquals("1:00.00", 60.0.formatHundredths())
        assertEquals("2:00.00", 120.0.formatHundredths())
        assertEquals("59:59.00", 3599.0.formatHundredths())
        assertEquals("2:00.99", 120.99.formatHundredths())
        assertEquals("2:00.99", 120.994.formatHundredths())
        assertEquals("2:01.00", 120.995.formatHundredths())
    }
}