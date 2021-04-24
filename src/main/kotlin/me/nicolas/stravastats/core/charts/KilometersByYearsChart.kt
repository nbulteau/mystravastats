package me.nicolas.stravastats.core.charts

import kscience.plotly.*
import kscience.plotly.models.*
import me.nicolas.stravastats.business.*
import java.time.Month
import java.time.format.TextStyle
import java.util.*

class KilometersByYearsChart {

    companion object {

        fun buildKilometersByYearsCharts(clientId: String, activities: List<Activity>) {

            // group by year
            val activitiesByYear = activities
                .groupBy { activity ->
                    activity.startDateLocal.subSequence(0, 4).toString()
                }.toSortedMap()

            val runByYears = activitiesByYear.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Run }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val bikeByYears = activitiesByYear.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Ride }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val inLineSkateByYears = activitiesByYear.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == InlineSkate }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val hikeByYears = activitiesByYear.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Hike }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }

            val plot = Plotly.grid {
                buildBarModeStackPlot(runByYears, bikeByYears, inLineSkateByYears, hikeByYears)
                buildBarModeGroupPlot(runByYears, bikeByYears, inLineSkateByYears, hikeByYears)
                buildCumulativePlot(runByYears, bikeByYears, inLineSkateByYears, hikeByYears)
            }

            plot.makeFile()
        }

        private fun PlotGrid.buildBarModeStackPlot(
            runByYears: Map<String, Double>,
            bikeByYears: Map<String, Double>,
            inLineSkateByYears: Map<String, Double>,
            hikeByYears: Map<String, Double>
        ) {
            plot(row = 1, width = 6) {
                traces(
                    buildBar(runByYears, Run),
                    buildBar(bikeByYears, Ride),
                    buildBar(inLineSkateByYears, InlineSkate),
                    buildBar(hikeByYears, Hike)
                )

                layout {
                    barmode = BarMode.stack
                    title = "Cumulative kilometers by year"

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
            runByYears: Map<String, Double>,
            bikeByYears: Map<String, Double>,
            inLineSkateByYears: Map<String, Double>,
            hikeByYears: Map<String, Double>
        ) {
            plot(row = 1, width = 6) {
                traces(
                    buildBar(runByYears, Run),
                    buildBar(bikeByYears, Ride),
                    buildBar(inLineSkateByYears, InlineSkate),
                    buildBar(hikeByYears, Hike)
                )

                layout {
                    barmode = BarMode.group
                    title = "Kilometers by year"

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
            runByYears: Map<String, Double>,
            bikeByYears: Map<String, Double>,
            inLineSkateByYears: Map<String, Double>,
            hikeByYears: Map<String, Double>
        ) {
            plot(row = 2, width = 12) {
                traces(
                    buildLine(getCumulativeSum(runByYears), Run),
                    buildLine(getCumulativeSum(bikeByYears), Ride),
                    buildLine(getCumulativeSum(inLineSkateByYears), InlineSkate),
                    buildLine(getCumulativeSum(hikeByYears), Hike)
                )

                layout {
                    //barmode = BarMode.stack
                    title = "Cumulative kilometers by year (sport by sport)"

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

        private fun buildBar(activities: Map<String, Double>, type: String): Bar {
            val activitiesByYears = addYearWithoutActivity(activities)

            return Bar {
                x.set(activitiesByYears.keys)
                y.set(activitiesByYears.values)
                name = type
            }
        }

        private fun addYearWithoutActivity(activities: Map<String, Double>): Map<String, Double> {
            val activitiesByYears = activities.toMutableMap()
            val min = activitiesByYears.keys.minOf { it.toInt() }
            val max = activitiesByYears.keys.maxOf { it.toInt() }

            // Add years without activities
            (min..max).forEach {
                if (!activitiesByYears.contains("$it")) {
                    activitiesByYears["$it"] = 0.0
                }
            }
            return activitiesByYears
        }

        private fun buildLine(activities: Map<String, Double>, type: String): Scatter {
            val activitiesByYears = addYearWithoutActivity(activities)

            return Scatter {
                x.set(activitiesByYears.keys)
                y.set(activitiesByYears.values)
                name = type
            }
        }

        private fun getCumulativeSum(activities: Map<String, Double>): Map<String, Double> {
            var sum = 0.0
            return activities.mapValues { (_, value) -> sum += value; sum }
        }
    }
}