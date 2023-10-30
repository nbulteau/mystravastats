package me.nicolas.stravastats.service

import me.nicolas.stravastats.TestHelper
import me.nicolas.stravastats.business.Activity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ActivityHelperTest {

    @Test
    fun `groupActivitiesByYear 1 test`() {
        // GIVEN
        val activities = emptyList<Activity>()

        // WHEN
        val result = ActivityHelper.groupActivitiesByYear(activities)

        // THEN
        Assertions.assertEquals(0, result.size)
    }

    @Test
    fun `groupActivitiesByYear 2 test`() {
        val activities = TestHelper.loadActivities()
        val result = ActivityHelper.groupActivitiesByYear(activities)
        Assertions.assertEquals(2, result.size)
    }

    @Test
    fun `groupActivitiesByMonth 1 test`() {

        val activities = emptyList<Activity>()
        val result = ActivityHelper.groupActivitiesByMonth(activities)
        Assertions.assertEquals(12, result.size)
    }

    @Test
    fun `groupActivitiesByMonth 2 test`() {
        val activities = TestHelper.loadActivities()
        val result = ActivityHelper.groupActivitiesByMonth(activities)
        Assertions.assertEquals(12, result.size)
    }

    @Test
    fun `groupActivitiesByDay 1 test`() {

        val activities = emptyList<Activity>()
        val result = ActivityHelper.groupActivitiesByDay(activities, 2021)
        Assertions.assertEquals(365, result.size)
    }

    @Test
    fun `groupActivitiesByDay 2 test`() {
        val activities = TestHelper.loadActivities()
        val result = ActivityHelper.groupActivitiesByDay(activities, 2021)
        Assertions.assertEquals(365, result.size)
    }

    @Test
    fun `groupActivitiesByWeek 1 test`() {

        val activities = emptyList<Activity>()
        val result = ActivityHelper.groupActivitiesByWeek(activities)

        Assertions.assertEquals(52, result.size)
    }

    @Test
    fun `groupActivitiesByWeek 2 test`() {
        val activities = TestHelper.loadActivities()
        val result = ActivityHelper.groupActivitiesByWeek(activities)

        Assertions.assertEquals(54, result.size) // 54 because the first week of the year is not complete
    }
}