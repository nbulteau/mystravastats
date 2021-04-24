package me.nicolas.stravastats.core.charts

import kscience.plotly.*
import kscience.plotly.models.*
import me.nicolas.stravastats.business.*
import java.time.Month
import java.time.format.TextStyle
import java.util.*

internal class KilometersByMonthsChart {

    companion object {

        fun buildKilometersByMonthsCharts(clientId: String, activities: List<Activity>, year: Int) {

            val activitiesByMonth = getActivitiesByMonth(activities)

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

            val plot = Plotly.grid {
                buildBarModeStackPlot(runByMonths, bikeByMonths, inLineSkateByMonths, hikeByMonths, clientId, year)
                buildBarModeGroupPlot(runByMonths, bikeByMonths, inLineSkateByMonths, hikeByMonths, clientId, year)
                buildCumulativePlot(runByMonths, bikeByMonths, inLineSkateByMonths, hikeByMonths, clientId, year)
            }

            plot.makeFile()
        }

        private fun PlotGrid.buildBarModeStackPlot(
            runByMonths: Map<String, Double>,
            bikeByMonths: Map<String, Double>,
            inLineSkateByMonths: Map<String, Double>,
            hikeByMonths: Map<String, Double>,
            clientId: String,
            year: Int
        ) {
            plot(row = 1, width = 6) {
                traces(
                    buildBar(runByMonths, Run),
                    buildBar(bikeByMonths, Ride),
                    buildBar(inLineSkateByMonths, InlineSkate),
                    buildBar(hikeByMonths, Hike)
                )

                layout {
                    barmode = BarMode.stack
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

        private fun PlotGrid.buildBarModeGroupPlot(
            runByMonths: Map<String, Double>,
            bikeByMonths: Map<String, Double>,
            inLineSkateByMonths: Map<String, Double>,
            hikeByMonths: Map<String, Double>,
            clientId: String,
            year: Int
        ) {
            plot(row = 1, width = 6) {
                traces(
                    buildBar(runByMonths, Run),
                    buildBar(bikeByMonths, Ride),
                    buildBar(inLineSkateByMonths, InlineSkate),
                    buildBar(hikeByMonths, Hike)
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

        private fun getActivitiesByMonth(activities: List<Activity>): SortedMap<String, List<Activity>> {
            val activitiesByMonth = activities
                .groupBy { activity -> activity.startDateLocal.subSequence(5, 7).toString() }.toMutableMap()

            // Add months without activities
            (1..12).forEach {
                if (!activitiesByMonth.contains("$it".padStart(2, '0'))) {
                    activitiesByMonth["$it".padStart(2, '0')] = emptyList()
                }
            }

            return activitiesByMonth.toSortedMap()
        }

        private fun PlotGrid.buildCumulativePlot(
            runByMonths: Map<String, Double>,
            bikeByMonths: Map<String, Double>,
            inLineSkateByMonths: Map<String, Double>,
            hikeByMonths: Map<String, Double>,
            clientId: String,
            year: Int
        ) {
            plot(row = 2, width = 12) {
                traces(
                    buildLine(getCumulativeSum(runByMonths), Run),
                    buildLine(getCumulativeSum(bikeByMonths), Ride),
                    buildLine(getCumulativeSum(inLineSkateByMonths), InlineSkate),
                    buildLine(getCumulativeSum(hikeByMonths), Hike)
                )

                layout {
                    //barmode = BarMode.stack
                    title = "Cumulative kilometers by month for $year (sport by sport)"

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

        private fun buildBar(activitiesByMonths: Map<String, Double>, type: String): Bar {
            val sumKms = activitiesByMonths.values.toMutableList()
            (sumKms.size..12).forEach { _ -> sumKms.add(0.0) }

            return Bar {
                x.set(Month.values().map { it.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()) })
                y.set(sumKms)
                name = type
            }
        }

        private fun buildLine(activitiesByMonths: Map<String, Double>, type: String): Scatter {
            val sumKms = activitiesByMonths.values.toMutableList()
            (sumKms.size..12).forEach { _ -> sumKms.add(sumKms.maxOf { it }) }

            return Scatter {
                x.set(Month.values().map { it.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()) })
                y.set(sumKms)
                name = type
            }
        }
    }
}