package me.nicolas.stravastats.core

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.charts.KilometersByMonthsChart.Companion.buildKilometersByMonthsCharts
import me.nicolas.stravastats.core.charts.KilometersByYearsChart.Companion.buildKilometersByYearsCharts
import me.nicolas.stravastats.core.charts.KilometersForAYearChart.Companion.buildKilometersForAYearCharts


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
                buildKilometersByMonthsCharts(map.value, map.key.toInt())
                buildKilometersForAYearCharts(map.value, map.key.toInt())
                nbYears++
            }

        if (nbYears > 1) {
            buildKilometersByYearsCharts(clientId, activities)
        }
    }
}