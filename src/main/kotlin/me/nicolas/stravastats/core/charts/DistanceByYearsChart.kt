package me.nicolas.stravastats.core.charts

import kscience.plotly.*
import kscience.plotly.models.*
import me.nicolas.stravastats.business.*

internal class DistanceByYearsChart(activities: List<Activity>): Chart() {

    private val activitiesByYear = groupActivitiesByYear(activities)

    override fun build() {
        val runByYears = cumulativeDistance(activitiesByYear, Run)
        val rideByYears = cumulativeDistance(activitiesByYear, Ride)
        val inLineSkateByYears = cumulativeDistance(activitiesByYear, InlineSkate)
        val hikeByYears = cumulativeDistance(activitiesByYear, Hike)

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
                buildLine(cumulativeSum(runByYears), Run),
                buildLine(cumulativeSum(bikeByYears), Ride),
                buildLine(cumulativeSum(inLineSkateByYears), InlineSkate),
                buildLine(cumulativeSum(hikeByYears), Hike)
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
}