package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.AthleteRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EddingtonStatisticTest {

    @Test
    fun `eddingtonTest 2`() {

        // Given
        val activities = listOf(
            buildActivity("2020-01-01T12:16:22Z", 1000.0),
            buildActivity("2020-02-02T12:13:52Z", 2000.0),
            buildActivity("2020-02-03T12:13:52Z", 3000.0),
            buildActivity("2020-02-04T12:13:52Z", 4000.0),
        )

        // When
        val eddingtonStatistic = EddingtonStatistic(activities)

        //Then
        assertEquals(2, eddingtonStatistic.eddingtonNumber)
        assertEquals(listOf(4, 3, 2, 1), eddingtonStatistic.nbDaysDistanceIsReached)
    }
    @Test
    fun `eddingtonTest 4`() {

        // Given
        val activities = listOf(
            buildActivity("2020-01-01T12:16:22Z", 2000.0),
            buildActivity("2020-01-01T17:13:52Z", 2000.0),
            buildActivity("2020-02-02T12:13:52Z", 4000.0),
            buildActivity("2020-02-03T12:13:52Z", 4000.0),
            buildActivity("2020-02-04T12:13:52Z", 4000.0),
        )

        // When
        val eddingtonStatistic = EddingtonStatistic(activities)

        //Then
        assertEquals(4, eddingtonStatistic.eddingtonNumber)
        assertEquals(listOf(4, 4, 4, 4), eddingtonStatistic.nbDaysDistanceIsReached)
    }

    @Test
    fun `eddingtonTest 3`() {

        // Given
        val activities = listOf(
            buildActivity("2020-01-01T12:16:22Z", 2000.0),
            buildActivity("2020-01-01T17:13:52Z", 2000.0),
            buildActivity("2020-02-02T12:13:52Z", 4000.0),
            buildActivity("2020-02-03T12:13:52Z", 4000.0),
        )

        // When
        val eddingtonStatistic = EddingtonStatistic(activities)

        //Then
        assertEquals(3, eddingtonStatistic.eddingtonNumber)
        assertEquals(listOf(3, 3, 3, 3), eddingtonStatistic.nbDaysDistanceIsReached)
    }



    private fun buildActivity(startDateLocal: String, distance: Double) = Activity(
        athlete = AthleteRef(0),
        averageSpeed = 0.0,
        averageCadence = 0.0,
        averageHeartrate = 0.0,
        maxHeartrate = 0.0,
        averageWatts = 0.0,
        commute = false,
        distance = distance,
        elapsedTime = 0,
        elevHigh = 0.0,
        id = 0,
        kilojoules = 0.0,
        maxSpeed = 0.0,
        movingTime = 0,
        name = "",
        startDate = "",
        startDateLocal = startDateLocal,
        startLatlng = null,
        totalElevationGain = 0.0,
        type = "",
        uploadId = 0,
        deviceWatts = false,
        weightedAverageWatts = 0
    )
}


