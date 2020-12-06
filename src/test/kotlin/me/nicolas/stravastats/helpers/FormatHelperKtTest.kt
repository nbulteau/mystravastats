package me.nicolas.stravastats.helpers

import me.nicolas.stravastats.business.formatSeconds
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FormatHelperKtTest {

    @Test
    fun `Int formatSeconds`() {

        assertEquals("00s", 0.formatSeconds())
        assertEquals("59s", 59.formatSeconds())
        assertEquals("01m 00s", 60.formatSeconds())
        assertEquals("02m 00s", 120.formatSeconds())
        assertEquals("59m 59s", 3599.formatSeconds())
    }

    @Test
    fun `Double formatSeconds`() {

        assertEquals("0'00", 0.0.formatSeconds())
        assertEquals("0'59", 59.0.formatSeconds())
        assertEquals("1'00", 60.0.formatSeconds())
        assertEquals("2'00", 120.0.formatSeconds())
        assertEquals("59'59", 3599.0.formatSeconds())
        assertEquals("2'00", 120.99.formatSeconds())
        assertEquals("2'00", 120.994.formatSeconds())
        assertEquals("2'01", 120.995.formatSeconds())
        assertEquals("59'59", 3599.994.formatSeconds())
    }
}