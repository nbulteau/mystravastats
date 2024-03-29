package me.nicolas.stravastats.service.charts

import me.nicolas.stravastats.business.*
import me.nicolas.stravastats.service.ActivityHelper
import space.kscience.plotly.*
import space.kscience.plotly.models.AxisType
import space.kscience.plotly.models.BarMode
import space.kscience.plotly.models.TraceOrder
import space.kscience.plotly.models.XAnchor

@Suppress("DEPRECATION")
@UnstablePlotlyAPI
internal class ForAYearChart(activities: List<Activity>, val year: Int) : Chart() {

    private val activitiesForYear = activities.filter { activity -> activity.startDateLocal.subSequence(0, 4).toString().toInt() == year }

    private val activitiesByMonth = ActivityHelper.groupActivitiesByMonth(activitiesForYear)

    private val activitiesByWeek = ActivityHelper.groupActivitiesByWeek(activitiesForYear)

    private val activitiesByDay = ActivityHelper.groupActivitiesByDay(activitiesForYear, year)

    override fun build() {
        val plotlyPage = Plotly.grid {
            buildBarModeGroupByDistanceMonthsPlot(
                xAxis = "months",
                yAxis = "Distance",
                unit = "km/h",
                runByMonths = ActivityHelper.sumDistanceByType(activitiesByMonth, Run),
                rideByMonths = ActivityHelper.sumDistanceByType(activitiesByMonth, Ride),
                inLineSkateByMonths = ActivityHelper.sumDistanceByType(activitiesByMonth, InlineSkate),
                hikeByMonths = ActivityHelper.sumDistanceByType(activitiesByMonth, Hike),
                year
            )
            buildBarModeStackDistanceByDayPlot(
                xAxis = "days",
                yAxis = "Distance",
                unit = "km/h",
                runByDays = ActivityHelper.sumDistanceByType(activitiesByDay, Run),
                rideByDays = ActivityHelper.sumDistanceByType(activitiesByDay, Ride),
                inLineSkateByDays = ActivityHelper.sumDistanceByType(activitiesByDay, InlineSkate),
                hikeByDays = ActivityHelper.sumDistanceByType(activitiesByDay, Hike),
                year
            )
            buildBarModeStackDistanceByWeekPlot(
                xAxis = "weeks",
                yAxis = "Distance",
                unit = "km/h",
                runByDays = ActivityHelper.sumDistanceByType(activitiesByWeek, Run),
                rideByDays = ActivityHelper.sumDistanceByType(activitiesByWeek, Ride),
                inLineSkateByDays = ActivityHelper.sumDistanceByType(activitiesByWeek, InlineSkate),
                hikeByDays = ActivityHelper.sumDistanceByType(activitiesByWeek, Hike),
                year
            )
            buildBarModeGroupByDistanceMonthsPlot(
                xAxis = "months",
                yAxis = "Elevation",
                unit = "m",
                runByMonths = ActivityHelper.sumElevationByType(activitiesByMonth, Run),
                rideByMonths = ActivityHelper.sumElevationByType(activitiesByMonth, Ride),
                inLineSkateByMonths = ActivityHelper.sumElevationByType(activitiesByMonth, InlineSkate),
                hikeByMonths = ActivityHelper.sumElevationByType(activitiesByMonth, Hike),
                year
            )
            buildLineByDayPlot(
                xAxis = "days",
                yAxis = "Average speed",
                unit = "km/h",
                runByDays = ActivityHelper.averageSpeedByType(activitiesByDay, Run),
                inLineSkateByDays = ActivityHelper.averageSpeedByType(activitiesByDay, InlineSkate),
                year
            )
            buildEddingtonNumberPlotByType(activitiesForYear, Run, row = 6, width = 6)
            buildEddingtonNumberPlotByType(activitiesForYear, Ride, row = 6, width = 6)
        }
        renderAndOpenBrowser(plotlyPage)
    }

    private fun PlotGrid.buildBarModeGroupByDistanceMonthsPlot(
        xAxis: String,
        yAxis: String,
        unit: String,
        runByMonths: Map<String, Double>,
        rideByMonths: Map<String, Double>,
        inLineSkateByMonths: Map<String, Double>,
        hikeByMonths: Map<String, Double>,
        year: Int
    ) {
        plot(row = 1, width = 12) {
            traces(
                buildBarByType(runByMonths, Run),
                buildBarByType(rideByMonths, Ride),
                buildBarByType(inLineSkateByMonths, InlineSkate),
                buildBarByType(hikeByMonths, Hike)
            )

            layout {
                barmode = BarMode.group
                title = "$yAxis by $xAxis for $year ($unit)"

                xaxis {
                    title = xAxis
                }
                yaxis {
                    title = yAxis
                }
                legend {
                    xanchor = XAnchor.left
                    bgcolor("#E2E2E2")
                    traceorder = TraceOrder.normal
                }
            }
        }
    }

    private fun PlotGrid.buildBarModeStackDistanceByWeekPlot(
        xAxis: String,
        yAxis: String,
        unit: String,
        runByDays: Map<String, Double>,
        rideByDays: Map<String, Double>,
        inLineSkateByDays: Map<String, Double>,
        hikeByDays: Map<String, Double>,
        year: Int
    ) {
        plot(row = 2, width = 12) {
            traces(
                buildBarByType(runByDays, Run),
                buildBarByType(rideByDays, Ride),
                buildBarByType(inLineSkateByDays, InlineSkate),
                buildBarByType(hikeByDays, Hike)
            )

            layout {
                barmode = BarMode.stack
                title = "$yAxis by $xAxis for $year ($unit)"

                xaxis {
                    title = xAxis
                    type = AxisType.category
                }
                yaxis {
                    title = yAxis
                }
                legend {
                    xanchor = XAnchor.right
                    bgcolor("#E2E2E2")
                    traceorder = TraceOrder.normal
                }
            }
        }
    }

    private fun PlotGrid.buildBarModeStackDistanceByDayPlot(
        xAxis: String,
        yAxis: String,
        unit: String,
        runByDays: Map<String, Double>,
        rideByDays: Map<String, Double>,
        inLineSkateByDays: Map<String, Double>,
        hikeByDays: Map<String, Double>,
        year: Int
    ) {
        plot(row = 2, width = 12) {
            traces(
                buildBarByType(runByDays, Run),
                buildBarByType(rideByDays, Ride),
                buildBarByType(inLineSkateByDays, InlineSkate),
                buildBarByType(hikeByDays, Hike)
            )

            layout {
                barmode = BarMode.stack
                title = "$yAxis by $xAxis for $year ($unit)"

                xaxis {
                    title = xAxis
                    type = AxisType.category
                }
                yaxis {
                    title = yAxis
                }
                legend {
                    xanchor = XAnchor.right
                    bgcolor("#E2E2E2")
                    traceorder = TraceOrder.normal
                }
            }
        }
    }

    private fun PlotGrid.buildLineByDayPlot(
        xAxis: String,
        yAxis: String,
        unit: String,
        runByDays: Map<String, Double>,
        inLineSkateByDays: Map<String, Double>,
        year: Int
    ) {
        plot(row = 2, width = 12) {
            traces(
                buildLineByType(runByDays, Run),
                buildLineByType(inLineSkateByDays, InlineSkate),
            )

            layout {
                //barmode = BarMode.stack
                title = "$yAxis by $xAxis for $year ($unit)"

                xaxis {
                    title = "Day"
                    type = AxisType.category
                }
                yaxis {
                    title = "Km/h"
                }
                legend {
                    xanchor = XAnchor.right
                    bgcolor("#E2E2E2")
                    traceorder = TraceOrder.normal
                }
            }
        }
    }
}

