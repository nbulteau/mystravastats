package me.nicolas.stravastats.core

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.charts.KilometersByMonthsChart.Companion.buildKilometersByMonthsCharts
import me.nicolas.stravastats.core.charts.KilometersByYearsChart.Companion.buildKilometersByYearsCharts


internal class ChartsBuilder {

    fun buildCharts(clientId: String, activities: List<Activity>) {
        // group by year
        activities
            .groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            }
            .forEach { map: Map.Entry<String, List<Activity>> ->
                // year by year
                buildKilometersByMonthsCharts(clientId, map.value, map.key.toInt())
            }

        buildKilometersByYearsCharts(clientId, activities)
    }
}