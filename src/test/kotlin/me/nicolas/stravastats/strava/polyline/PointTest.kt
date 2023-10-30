package me.nicolas.stravastats.strava.polyline

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PointTest {
    @Test
    fun toGeoJSON() {
        // GIVEN
        val points = listOf(
            Point(48.1576, -1.5872),
            Point(48.1576, -1.5872),
            Point(48.1576, -1.5872)
        )

        // WHEN
        val geoJSON = Point.toGeoJSON(points)

        // THEN
        assertEquals("[[-1.5872,48.1576],[-1.5872,48.1576],[-1.5872,48.1576]]", geoJSON)
    }

    @Test
    fun toGeoJSONUsiingPoint() {
        // GIVEN
        val point = Point(48.1576, -1.5872)

        // WHEN
        val geoJSON = Point.toGeoJSON(point)

        // THEN
        assertEquals("[-1.5872,48.1576]", geoJSON)
    }

    @Test
    fun equals() {
        // GIVEN
        val point1 = Point(48.1576, -1.5872)
        val point2 = Point(48.1576, -1.5872)
        // With a precision < to 1e-3 degree
        val point3 = Point(48.1576, -1.5873)

        // WHEN
        val equals1 = point1 == point2
        val equals2 = point1 == point3

        // THEN
        assertTrue(equals1)
        assertTrue(equals2)
    }
}