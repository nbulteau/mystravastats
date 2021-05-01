package me.nicolas.stravastats.core

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.charts.ForAYearChart
import me.nicolas.stravastats.core.charts.ByYearsChart


internal class ChartsService {

    fun buildCharts(activities: List<Activity>) {
        var nbYears = 0
        // group by year
        activities
            .groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            }
            .forEach { map: Map.Entry<String, List<Activity>> ->
                ForAYearChart(map.value, map.key.toInt()).build()
                nbYears++
            }

        if (nbYears > 1) {
            ByYearsChart(activities).build()
        }
    }
}