package me.nicolas.stravastats.core.charts

import kscience.plotly.*
import kscience.plotly.models.*
import me.nicolas.stravastats.business.*
import java.time.Month
import java.time.format.TextStyle
import java.util.*

internal class ByMonthsChart {

    companion object {

        fun build(activities: List<Activity>, year: Int) {

            val activitiesByMonth = ChartHelper.groupActivitiesByMonth(activities)
            val runByMonths = ChartHelper.cumulativeDistance(activitiesByMonth, Run)
            val bikeByMonths = ChartHelper.cumulativeDistance(activitiesByMonth, Ride)
            val inLineSkateByMonths = ChartHelper.cumulativeDistance(activitiesByMonth, InlineSkate)
            val hikeByMonths = ChartHelper.cumulativeDistance(activitiesByMonth, Hike)

            val activitiesByDay = ChartHelper.groupActivitiesByDay(activities, year)
            val runByDays = ChartHelper.cumulativeDistance(activitiesByDay, Run)
            val rideByDays = ChartHelper.cumulativeDistance(activitiesByDay, Ride)
            val inLineSkateByDays = ChartHelper.cumulativeDistance(activitiesByDay, InlineSkate)
            val hikeByDays = ChartHelper.cumulativeDistance(activitiesByDay, Hike)

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
                    buildBarByMonth(runByMonths, Run),
                    buildBarByMonth(bikeByMonths, Ride),
                    buildBarByMonth(inLineSkateByMonths, InlineSkate),
                    buildBarByMonth(hikeByMonths, Hike)
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

        private fun buildBarByMonth(activitiesByMonths: Map<String, Double>, type: String): Bar {
            val sumKms = activitiesByMonths.values.toMutableList()
            for (i in sumKms.size..12) {
                sumKms.add(0.0)
            }

            return Bar {
                x.set(Month.values().map { it.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()) })
                y.set(sumKms)
                name = type
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
                    buildBar(runByDays, Run),
                    buildBar(bikeByDays, Ride),
                    buildBar(inLineSkateByDays, InlineSkate),
                    buildBar(hikeByDays, Hike)
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

        private fun buildBar(activitiesByDays: Map<String, Double>, type: String) =
            Bar {
                x.set(activitiesByDays.keys)
                y.set(activitiesByDays.values)
                name = type
            }
    }
}
