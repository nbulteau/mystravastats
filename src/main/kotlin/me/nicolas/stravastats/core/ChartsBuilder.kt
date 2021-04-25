package me.nicolas.stravastats.core

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.charts.KilometersByMonthsChart.Companion.buildCharts
import me.nicolas.stravastats.core.charts.KilometersByYearsChart.Companion.buildKilometersByYearsCharts


internal class ChartsBuilder {

    fun buildCharts(clientId: String, activities: List<Activity>) {
        var nbYears = 0
        // group by year
        activities
            .groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            }
            .forEach { map: Map.Entry<String, List<Activity>> ->
                // year by year
                buildCharts(map.value, map.key.toInt())
                nbYears++
            }

        if (nbYears > 1) {
            buildKilometersByYearsCharts(clientId, activities)
        }
    }
}