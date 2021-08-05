package me.nicolas.stravastats.service

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.charts.ByYearsChart
import me.nicolas.stravastats.service.charts.ForAYearChart
import space.kscience.plotly.UnstablePlotlyAPI


internal class ChartsService {

    @OptIn(UnstablePlotlyAPI::class)
    fun buildCharts(activities: List<Activity>, year: Int) {

        ForAYearChart(activities, year).build()

        ByYearsChart(activities).build()
    }
}