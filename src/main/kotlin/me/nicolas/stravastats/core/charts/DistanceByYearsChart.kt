package me.nicolas.stravastats.core.charts

import kscience.plotly.*
import kscience.plotly.models.*
import me.nicolas.stravastats.business.*

internal class DistanceByYearsChart(activities: List<Activity>): Chart() {

    private val activitiesByYear = groupActivitiesByYear(activities)

    override fun build() {
        val runByYears = sumDistance(activitiesByYear, Run)
        val rideByYears = sumDistance(activitiesByYear, Ride)
        val inLineSkateByYears = sumDistance(activitiesByYear, InlineSkate)
        val hikeByYears = sumDistance(activitiesByYear, Hike)

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
                buildBarByType(runByYears, Run),
                buildBarByType(bikeByYears, Ride),
                buildBarByType(inLineSkateByYears, InlineSkate),
                buildBarByType(hikeByYears, Hike)
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
                buildBarByType(runByYears, Run),
                buildBarByType(bikeByYears, Ride),
                buildBarByType(inLineSkateByYears, InlineSkate),
                buildBarByType(hikeByYears, Hike)
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
                buildLineByType(cumulativeValue(runByYears), Run),
                buildLineByType(cumulativeValue(bikeByYears), Ride),
                buildLineByType(cumulativeValue(inLineSkateByYears), InlineSkate),
                buildLineByType(cumulativeValue(hikeByYears), Hike)
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