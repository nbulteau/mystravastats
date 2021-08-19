package me.nicolas.stravastats.service

import com.fasterxml.jackson.databind.ObjectMapper
import me.nicolas.stravastats.business.Activity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

internal class ActivityHelperTest {

    @Test
    fun `groupActivitiesByYear 1 test`() {

        val activities = emptyList<Activity>()
        val result = ActivityHelper.groupActivitiesByYear(activities)
        Assertions.assertEquals(0, result.size)
    }

    @Test
    fun `groupActivitiesByYear 2 test`() {
        val url = Thread.currentThread().contextClassLoader.getResource("activities.json")
        val jsonFile = File(url.path)

        val activities = ObjectMapper().readValue(jsonFile, Array<Activity>::class.java).toList()
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
        val url = Thread.currentThread().contextClassLoader.getResource("activities.json")
        val jsonFile = File(url.path)

        val activities = ObjectMapper().readValue(jsonFile, Array<Activity>::class.java).toList()
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
        val url = Thread.currentThread().contextClassLoader.getResource("activities.json")
        val jsonFile = File(url.path)

        val activities = ObjectMapper().readValue(jsonFile, Array<Activity>::class.java).toList()
        val result = ActivityHelper.groupActivitiesByDay(activities, 2021)
        Assertions.assertEquals(365, result.size)
    }
}