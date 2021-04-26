package me.nicolas.stravastats.core

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.charts.ByMonthsChart.Companion.buildKilometersByMonthsCharts
import me.nicolas.stravastats.core.charts.ByYearsChart.Companion.buildKilometersByYearsCharts
import me.nicolas.stravastats.core.charts.RideOverYearsChart


internal class ChartsService {

    fun buildCharts(activities: List<Activity>) {
        var nbYears = 0
        // group by year
        activities
            .groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            }
            .forEach { map: Map.Entry<String, List<Activity>> ->
                buildKilometersByMonthsCharts(map.value, map.key.toInt())
                nbYears++
            }

        if (nbYears > 1) {
            buildKilometersByYearsCharts(activities)
            RideOverYearsChart(activities).build()
        }
    }
}