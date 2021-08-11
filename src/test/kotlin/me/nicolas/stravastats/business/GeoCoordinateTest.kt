package me.nicolas.stravastats.business


import me.nicolas.stravastats.business.badges.LocationBadge.Companion.COL_AGNEL
import me.nicolas.stravastats.business.badges.LocationBadge.Companion.COL_D_IZOARD
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class GeoCoordinateTest {

    @Test
    fun haversineInM1() {

        val result = COL_AGNEL.geoCoordinate.haversineInM(COL_D_IZOARD.geoCoordinate.latitude, COL_D_IZOARD.geoCoordinate.longitude)
        Assertions.assertEquals(result, result)
    }

    @Test
    fun haversineInM2() {

        val result = COL_AGNEL.geoCoordinate.haversineInM(COL_AGNEL.geoCoordinate.latitude, COL_AGNEL.geoCoordinate.latitude)
        Assertions.assertEquals(0, result)
    }
}