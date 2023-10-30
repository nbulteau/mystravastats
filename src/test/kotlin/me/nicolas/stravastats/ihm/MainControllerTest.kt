package me.nicolas.stravastats.ihm

import javafx.collections.FXCollections
import me.nicolas.stravastats.TestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MainControllerTest {

    @Test
    fun `build distance by days series for ride activity in 2020`() {
        // GIVEN
        val activities = TestHelper.loadActivities()
        val activityObservableList = FXCollections.observableArrayList(activities)
        val mainController = MainController("clientId", activityObservableList)

        // WHEN
        val rideDistanceByDaysFor2020 = mainController.buildDistanceByDaysSeries("Ride", 2020)

        // THEN
        // 366 days in 2020
        assertEquals(366, rideDistanceByDaysFor2020.size)
        // 39 ride active days in 2020
        assertEquals(39, rideDistanceByDaysFor2020.filter { it.yValue.toDouble() > 0.0 }.size)
    }

    @Test
    fun `build distance by days series for run activity in 2020`() {
        // GIVEN
        val activities = TestHelper.loadActivities()
        val activityObservableList = FXCollections.observableArrayList(activities)
        val mainController = MainController("clientId", activityObservableList)

        // WHEN
        val rideDistanceByDaysFor2020 = mainController.buildDistanceByDaysSeries("Run", 2020)

        // THEN
        // 366 days in 2020
        assertEquals(366, rideDistanceByDaysFor2020.size)
        // 51 run active days in 2020
        assertEquals(51, rideDistanceByDaysFor2020.filter { it.yValue.toDouble() > 0.0 }.size)
    }
}