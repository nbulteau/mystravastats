package me.nicolas.stravastats.core.charts

import kscience.plotly.*
import kscience.plotly.models.AxisType
import kscience.plotly.models.BarMode
import kscience.plotly.models.TraceOrder
import kscience.plotly.models.XAnchor
import me.nicolas.stravastats.business.*

internal class ForAYearChart(activities: List<Activity>, val year: Int) : Chart() {

    private val activitiesByMonth = groupActivitiesByMonth(activities)

    private val activitiesByDay = groupActivitiesByDay(activities, year)

    override fun build() {
        Plotly.grid {
            buildBarModeGroupPlot(
                xAxis = "months",
                yAxis = "Distance",
                unit = "km/h",
                runByMonths = sumDistance(activitiesByMonth, Run),
                rideByMonths = sumDistance(activitiesByMonth, Ride),
                inLineSkateByMonths = sumDistance(activitiesByMonth, InlineSkate),
                hikeByMonths = sumDistance(activitiesByMonth, Hike),
                year
            )
            buildBarModeStackByDayPlot(
                xAxis = "days",
                yAxis = "Distance",
                unit = "km/h",
                runByDays = sumDistance(activitiesByDay, Run),
                rideByDays = sumDistance(activitiesByDay, Ride),
                inLineSkateByDays = sumDistance(activitiesByDay, InlineSkate),
                hikeByDays = sumDistance(activitiesByDay, Hike),
                year
            )
            buildLineByDayPlot(
                xAxis = "days",
                yAxis = "Average speed",
                unit = "km/h",
                runByDays = averageSpeed(activitiesByDay, Run),
                inLineSkateByDays = averageSpeed(activitiesByDay, InlineSkate),
                year
            )
        }.makeFile()
    }

    private fun PlotGrid.buildBarModeGroupPlot(
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

    private fun PlotGrid.buildBarModeStackByDayPlot(
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

