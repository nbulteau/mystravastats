package me.nicolas.stravastats.core.charts

import kscience.plotly.*
import kscience.plotly.models.AxisType
import kscience.plotly.models.BarMode
import kscience.plotly.models.TraceOrder
import kscience.plotly.models.XAnchor
import me.nicolas.stravastats.business.*

internal class DistanceForAYearChart(activities: List<Activity>, val year: Int): Chart() {

    private val activitiesByMonth = groupActivitiesByMonth(activities)

    private val activitiesByDay = groupActivitiesByDay(activities, year)

    override fun build() {
        val runByMonths = sumDistance(activitiesByMonth, Run)
        val bikeByMonths = sumDistance(activitiesByMonth, Ride)
        val inLineSkateByMonths = sumDistance(activitiesByMonth, InlineSkate)
        val hikeByMonths = sumDistance(activitiesByMonth, Hike)

        val runByDays = sumDistance(activitiesByDay, Run)
        val rideByDays = sumDistance(activitiesByDay, Ride)
        val inLineSkateByDays = sumDistance(activitiesByDay, InlineSkate)
        val hikeByDays = sumDistance(activitiesByDay, Hike)

        val plot = Plotly.grid {
            buildBarModeGroupPlot(runByMonths, bikeByMonths, inLineSkateByMonths, hikeByMonths, year)
            buildBarModeStackByDayPlot(runByDays, rideByDays, inLineSkateByDays, hikeByDays, year)
        }

        plot.makeFile()
    }

    private fun PlotGrid.buildBarModeGroupPlot(
        runByMonths: Map<String, Double>,
        bikeByMonths: Map<String, Double>,
        inLineSkateByMonths: Map<String, Double>,
        hikeByMonths: Map<String, Double>,
        year: Int
    ) {
        plot(row = 1, width = 12) {
            traces(
                buildBarByType(runByMonths, Run),
                buildBarByType(bikeByMonths, Ride),
                buildBarByType(inLineSkateByMonths, InlineSkate),
                buildBarByType(hikeByMonths, Hike)
            )

            layout {
                barmode = BarMode.group
                title = "Kilometers by month for $year"

                xaxis {
                    title = "Month"
                }
                yaxis {
                    title = "Km"
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
        runByDays: Map<String, Double>,
        bikeByDays: Map<String, Double>,
        inLineSkateByDays: Map<String, Double>,
        hikeByDays: Map<String, Double>,
        year: Int
    ) {
        plot(row = 2, width = 12) {
            traces(
                buildBarByType(runByDays, Run),
                buildBarByType(bikeByDays, Ride),
                buildBarByType(inLineSkateByDays, InlineSkate),
                buildBarByType(hikeByDays, Hike)
            )

            layout {
                barmode = BarMode.stack
                title = "kilometers by day for $year"

                xaxis {
                    title = "Day"
                    type = AxisType.category
                }
                yaxis {
                    title = "Km"
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

