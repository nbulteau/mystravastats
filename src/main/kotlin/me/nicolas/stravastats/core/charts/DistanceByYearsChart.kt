package me.nicolas.stravastats.core.charts

import kscience.plotly.*
import kscience.plotly.models.*
import me.nicolas.stravastats.business.*

internal class DistanceByYearsChart(activities: List<Activity>) {

    private val activitiesByYear = ChartHelper.groupActivitiesByYear(activities)

    fun build() {
        var runByYears = ChartHelper.cumulativeDistance(activitiesByYear, Run)
        runByYears = addYearWithoutActivity(runByYears)
        var rideByYears = ChartHelper.cumulativeDistance(activitiesByYear, Ride)
        rideByYears = addYearWithoutActivity(rideByYears)
        var inLineSkateByYears = ChartHelper.cumulativeDistance(activitiesByYear, InlineSkate)
        inLineSkateByYears = addYearWithoutActivity(inLineSkateByYears)
        var hikeByYears = ChartHelper.cumulativeDistance(activitiesByYear, Hike)
        hikeByYears = addYearWithoutActivity(hikeByYears)

        val plot = Plotly.grid {
            buildBarModeStackPlot(runByYears, rideByYears, inLineSkateByYears, hikeByYears)
            buildBarModeGroupPlot(runByYears, rideByYears, inLineSkateByYears, hikeByYears)
            buildCumulativePlot(runByYears, rideByYears, inLineSkateByYears, hikeByYears)
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
                    type = AxisType.category
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
                    type = AxisType.category
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
                title = "Cumulative kilometers by year"

                xaxis {
                    title = "Month"
                    type = AxisType.category
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

    private fun buildBar(activities: Map<String, Double>, type: String) = Bar {
        x.set(activities.keys)
        y.set(activities.values)
        name = type
    }


    private fun buildLine(activities: Map<String, Double>, type: String) = Scatter {
        x.set(activities.keys)
        y.set(activities.values)
        name = type
    }


    private fun addYearWithoutActivity(activities: Map<String, Double>): Map<String, Double> {
        val activitiesByYears = activities.toMutableMap()
        val min = activitiesByYears.keys.minOf { it.toInt() }
        val max = activitiesByYears.keys.maxOf { it.toInt() }

        // Add years without activities
        for (year in min..max) {
            if (!activitiesByYears.contains("$year")) {
                activitiesByYears["$year"] = 0.0
            }
        }
        return activitiesByYears.toSortedMap()
    }

    private fun getCumulativeSum(activities: Map<String, Double>): Map<String, Double> {
        var sum = 0.0
        return activities.mapValues { (_, value) -> sum += value; sum }
    }
}