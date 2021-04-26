package me.nicolas.stravastats.core.charts

import kscience.plotly.*
import kscience.plotly.models.*
import me.nicolas.stravastats.business.*
import java.time.Month
import java.time.format.TextStyle
import java.util.*

internal class ByMonthsChart {

    companion object {

        fun buildKilometersByMonthsCharts(activities: List<Activity>, year: Int) {

            val activitiesByMonth = ChartHelper.getActivitiesByMonth(activities)

            val runByMonths = activitiesByMonth.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Run }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val bikeByMonths = activitiesByMonth.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Ride }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val inLineSkateByMonths = activitiesByMonth.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == InlineSkate }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val hikeByMonths = activitiesByMonth.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Hike }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }

            val activitiesByDay = ChartHelper.getActivitiesByDay(activities, year)

            val runByDays = activitiesByDay.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Run }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val rideByDays = activitiesByDay.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Ride }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val inLineSkateByDays = activitiesByDay.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == InlineSkate }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val hikeByDays = activitiesByDay.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Hike }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }


            val plot = Plotly.grid {
                buildBarModeGroupPlot(runByMonths, bikeByMonths, inLineSkateByMonths, hikeByMonths, year)
                buildCumulativePlot(runByMonths, bikeByMonths, inLineSkateByMonths, hikeByMonths, year)
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
            plot(row = 1, width = 6) {
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

        private fun PlotGrid.buildCumulativePlot(
            runByMonths: Map<String, Double>,
            bikeByMonths: Map<String, Double>,
            inLineSkateByMonths: Map<String, Double>,
            hikeByMonths: Map<String, Double>,
            year: Int
        ) {
            plot(row = 1, width = 6) {
                traces(
                    buildLine(getCumulativeSum(runByMonths), Run),
                    buildLine(getCumulativeSum(bikeByMonths), Ride),
                    buildLine(getCumulativeSum(inLineSkateByMonths), InlineSkate),
                    buildLine(getCumulativeSum(hikeByMonths), Hike)
                )

                layout {
                    title = "Cumulative kilometers by month for $year"

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

        private fun getCumulativeSum(activities: Map<String, Double>): Map<String, Double> {
            var sum = 0.0
            return activities.mapValues { (_, value) -> sum += value; sum }
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

        private fun buildLine(activitiesByMonths: Map<String, Double>, type: String): Scatter {
            val sumKms = activitiesByMonths.values.toMutableList()
            for (i in sumKms.size..12) {
                sumKms.add(sumKms.maxOf { it })
            }

            return Scatter {
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
