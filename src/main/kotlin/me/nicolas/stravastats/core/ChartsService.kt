package me.nicolas.stravastats.core

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Ride
import me.nicolas.stravastats.business.Run
import me.nicolas.stravastats.core.charts.DistanceForAYearChart
import me.nicolas.stravastats.core.charts.DistanceByYearsChart
import me.nicolas.stravastats.core.charts.ActivityByYearsChart


internal class ChartsService {

    fun buildCharts(activities: List<Activity>) {
        var nbYears = 0
        // group by year
        activities
            .groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            }
            .forEach { map: Map.Entry<String, List<Activity>> ->
                DistanceForAYearChart(map.value, map.key.toInt()).build()
                nbYears++
            }

        if (nbYears > 1) {
            DistanceByYearsChart(activities).build()
            ActivityByYearsChart(activities, Run).build()
            ActivityByYearsChart(activities, Ride).build()
        }
    }
}