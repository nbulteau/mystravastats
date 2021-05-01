package me.nicolas.stravastats.core.charts

import kscience.plotly.*
import kscience.plotly.models.*
import me.nicolas.stravastats.business.*
import hep.dataforge.values.Value
import java.time.LocalDate

internal class ByYearsChart(activities: List<Activity>) : Chart() {

    private val activitiesByYear = groupActivitiesByYear(activities)

    override fun build() {
        val runByYears = sumDistance(activitiesByYear, Run)
        val rideByYears = sumDistance(activitiesByYear, Ride)
        val inLineSkateByYears = sumDistance(activitiesByYear, InlineSkate)
        val hikeByYears = sumDistance(activitiesByYear, Hike)

        val plot = Plotly.grid {
            buildBarModeStackPlot(row = 1, width = 6, runByYears, rideByYears, inLineSkateByYears, hikeByYears)
            buildBarModeGroupPlot(row = 1, width = 6, runByYears, rideByYears, inLineSkateByYears, hikeByYears)
            buildCumulativePlot(row = 2, width = 12, runByYears, rideByYears, inLineSkateByYears, hikeByYears)
            buildCumulativeKilometers(row = 3, width = 12, activitiesByYear, Run)
            buildCumulativeKilometers(row = 4, width = 12, activitiesByYear, Ride)
            buildCumulativeElevation(row = 5, width = 12, activitiesByYear, Ride)
        }
        plot.makeFile()
    }

    private fun PlotGrid.buildBarModeStackPlot(
        row: Int,
        width: Int,
        runByYears: Map<String, Double>,
        bikeByYears: Map<String, Double>,
        inLineSkateByYears: Map<String, Double>,
        hikeByYears: Map<String, Double>
    ) {
        plot(row = row, width = width) {
            traces(
                buildBarByType(runByYears, Run),
                buildBarByType(bikeByYears, Ride),
                buildBarByType(inLineSkateByYears, InlineSkate),
                buildBarByType(hikeByYears, Hike)
            )

            layout {
                barmode = BarMode.stack
                title = "Kilometers by year"

                xaxis {
                    title = "Year"
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
        row: Int,
        width: Int,
        runByYears: Map<String, Double>,
        bikeByYears: Map<String, Double>,
        inLineSkateByYears: Map<String, Double>,
        hikeByYears: Map<String, Double>
    ) {
        plot(row = row, width = width) {
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
                    title = "Year"
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
        row: Int,
        width: Int,
        runByYears: Map<String, Double>,
        bikeByYears: Map<String, Double>,
        inLineSkateByYears: Map<String, Double>,
        hikeByYears: Map<String, Double>
    ) {
        val annotationsList = mutableListOf<Text>()

        val cumulativeRun = cumulativeValue(runByYears)
        val cumulativeRide = cumulativeValue(bikeByYears)
        val cumulativeInlineSkate = cumulativeValue(inLineSkateByYears)
        val cumulativeHike = cumulativeValue(hikeByYears)

        plot(row = row, width = width) {
            traces(
                buildLineByType(cumulativeRun, Run),
                buildLineByType(cumulativeRide, Ride),
                buildLineByType(cumulativeInlineSkate, InlineSkate),
                buildLineByType(cumulativeHike, Hike)
            )

            annotationsList.add(buildText(cumulativeRun))
            annotationsList.add(buildText(cumulativeRide))
            annotationsList.add(buildText(cumulativeInlineSkate))
            annotationsList.add(buildText(cumulativeHike))

            layout {
                title = "Cumulative kilometers by year"

                xaxis {
                    title = "Year"
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
                annotations = annotationsList
            }
        }
    }

    private fun PlotGrid.buildCumulativeKilometers(
        row: Int,
        width: Int,
        activitiesByYear: Map<String, List<Activity>>,
        activityType: String
    ) {

        val annotationsList = mutableListOf<Text>()


        plot(row = row, width = width) {
            for (year in 2010..LocalDate.now().year) {
                val activities = if (activitiesByYear[year.toString()] != null) {
                    activitiesByYear[year.toString()]?.filter { activity -> activity.type == activityType }!!
                } else {
                    continue
                }
                val activitiesByDay = groupActivitiesByDay(activities, year)
                val cumulativeDistance = cumulativeDistance(activitiesByDay)

                traces(
                    buildLineByYear(cumulativeDistance, year)
                )
                val text = Text {
                    xref = "x"
                    yref = "y"
                    x = Value.of(cumulativeDistance.keys.last())
                    y = Value.of(cumulativeDistance.values.last())
                    xanchor = XAnchor.left
                    yanchor = YAnchor.middle
                    text = "  %.0f km".format(cumulativeDistance.values.last())
                    font {
                        family = "Arial"
                        size = 12
                        color("black")
                    }
                    showarrow = false
                }
                annotationsList.add(text)
            }

            layout {
                title = "$activityType distance (km) by years"

                xaxis {
                    title = "Day"
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
                annotations = annotationsList
            }
        }
    }

    private fun PlotGrid.buildCumulativeElevation(
        row: Int,
        width: Int,
        activitiesByYear: Map<String, List<Activity>>,
        activityType: String
    ) {
        val annotationsList = mutableListOf<Text>()

        plot(row = row, width = width) {
            for (year in 2010..LocalDate.now().year) {
                val activities = if (activitiesByYear[year.toString()] != null) {
                    activitiesByYear[year.toString()]?.filter { activity -> activity.type == activityType }!!
                } else {
                    continue
                }
                val activitiesByDay = groupActivitiesByDay(activities, year)
                val cumulativeElevation = cumulativeElevation(activitiesByDay)

                traces(
                    buildLineByYear(cumulativeElevation, year)
                )
                val text = Text {
                    xref = "x"
                    yref = "y"
                    x = Value.of(cumulativeElevation.keys.last())
                    y = Value.of(cumulativeElevation.values.last())
                    xanchor = XAnchor.left
                    yanchor = YAnchor.middle
                    text = " %.0f m".format(cumulativeElevation.values.last())
                    font {
                        family = "Arial"
                        size = 12
                        color("black")
                    }
                    showarrow = false
                }
                annotationsList.add(text)
            }

            layout {
                title = "$activityType elevation (m) by years"

                xaxis {
                    title = "Day"
                    type = AxisType.category
                }
                yaxis {
                    title = "m"
                }
                legend {
                    xanchor = XAnchor.left
                    bgcolor("#E2E2E2")
                    traceorder = TraceOrder.normal
                }
                annotations = annotationsList
            }
        }
    }

    private fun buildText(activities: Map<String, Double>): Text {
        return Text {
            xref = "x"
            yref = "y"
            x = Value.of(activities.keys.last())
            y = Value.of(activities.values.last())
            xanchor = XAnchor.left
            yanchor = YAnchor.middle
            text = " %.0f km".format(activities.values.last())
            font {
                family = "Arial"
                size = 12
                color("black")
            }
            showarrow = false
        }
    }
}