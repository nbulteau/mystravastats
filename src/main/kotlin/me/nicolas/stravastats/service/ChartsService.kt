package me.nicolas.stravastats.service

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.charts.ForAYearChart
import me.nicolas.stravastats.service.charts.ByYearsChart
import space.kscience.plotly.UnstablePlotlyAPI


internal class ChartsService {

    @OptIn(UnstablePlotlyAPI::class)
    fun buildCharts(activities: List<Activity>, year: Int) {
        // group by year
        val activitiesGroupedByYear = activities
            .groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            }

        ForAYearChart(activitiesGroupedByYear[year.toString()] ?: emptyList(), year).build()

        //if (activitiesGroupedByYear.keys.size > 1) {
            ByYearsChart(activities).build()
        //}
    }
}