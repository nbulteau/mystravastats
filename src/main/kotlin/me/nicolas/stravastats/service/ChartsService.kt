package me.nicolas.stravastats.service

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.charts.ByYearsChart
import me.nicolas.stravastats.service.charts.ForAYearChart


internal class ChartsService {

    fun buildCharts(activities: List<Activity>, year: Int?) {

        if (year != null) {
            ForAYearChart(activities, year).build()
        }
        ByYearsChart(activities).build()
    }
}