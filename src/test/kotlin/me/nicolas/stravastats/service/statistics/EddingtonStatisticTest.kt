package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.AthleteRef
import me.nicolas.stravastats.business.Map
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EddingtonStatisticTest {

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
        val result = EddingtonStatistic(activities).eddingtonNumber

        //Then
        assertEquals(4, result)
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
        val result = EddingtonStatistic(activities).eddingtonNumber

        //Then
        assertEquals(3, result)
    }

    private fun buildActivity(startDateLocal: String, distance: Double) = Activity(
        achievementCount = 0,
        athlete = AthleteRef(0, 0),
        athleteCount = 0,
        averageSpeed = 0.0,
        averageCadence = 0.0,
        averageHeartrate = 0.0,
        maxHeartrate = 0.0,
        averageWatts = 0.0,
        commentCount = 0,
        commute = false,
        deviceWatts = false,
        displayHideHeartrateOption = false,
        distance = distance,
        elapsedTime = 0,
        elevHigh = 0.0,
        elevLow = 0.0,
        endLatlng = null,
        externalId = "",
        flagged = false,
        fromAcceptedTag = false,
        gearId = null,
        hasHeartrate = false,
        hasKudoed = false,
        heartrateOptOut = false,
        id = 0,
        kilojoules = 0.0,
        kudosCount = 0,
        locationCity = null,
        locationCountry = null,
        locationState = null,
        manual = false,
        map = Map("", 0, ""),
        maxSpeed = 0.0,
        movingTime = 0,
        name = "",
        photoCount = 0,
        prCount = 0,
        private = false,
        resourceState = 0,
        startDate = "",
        startDateLocal = startDateLocal,
        startLatitude = 0.0,
        startLatlng = null,
        startLongitude = 0.0,
        timezone = "",
        totalElevationGain = 0.0,
        totalPhotoCount = 0,
        trainer = false,
        type = "",
        uploadId = 0,
        uploadIdStr = "",
        utcOffset = 0.0,
        visibility = "",
        workoutType = 0
    )
}


