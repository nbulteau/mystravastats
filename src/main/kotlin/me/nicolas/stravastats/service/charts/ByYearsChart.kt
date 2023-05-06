package me.nicolas.stravastats.service.charts

import me.nicolas.stravastats.business.*
import me.nicolas.stravastats.service.ActivityHelper
import space.kscience.dataforge.meta.Value
import space.kscience.plotly.*
import space.kscience.plotly.models.*
import java.time.LocalDate

@Suppress("DEPRECATION")
@UnstablePlotlyAPI
internal class ByYearsChart(val activities: List<Activity>) : Chart() {

    private val activitiesByYear = ActivityHelper.groupActivitiesByYear(activities)

    override fun build() {
        val runByYears = ActivityHelper.sumDistanceByType(activitiesByYear, Run)
        val rideByYears = ActivityHelper.sumDistanceByType(activitiesByYear, Ride)
        val inLineSkateByYears = ActivityHelper.sumDistanceByType(activitiesByYear, InlineSkate)
        val hikeByYears = ActivityHelper.sumDistanceByType(activitiesByYear, Hike)

        val plotlyPage = Plotly.grid {
            buildBarModePlot(runByYears, rideByYears, inLineSkateByYears, hikeByYears, barMode = BarMode.stack)
            buildBarModePlot(runByYears, rideByYears, inLineSkateByYears, hikeByYears, barMode = BarMode.group)
            buildCumulativePlot(runByYears, rideByYears, inLineSkateByYears, hikeByYears)
            buildCumulativeKilometers(activitiesByYear, activityType = Run)
            buildCumulativeKilometers(activitiesByYear, activityType = Ride, row = 4)
            buildCumulativeElevation(activitiesByYear, activityType = Ride)
            buildEddingtonNumberPlotByType(activities, activityType = Run)
            buildEddingtonNumberPlotByType(activities, activityType = Ride)
        }
        renderAndOpenBrowser(plotlyPage)
    }

    private fun PlotGrid.buildBarModePlot(
        runByYears: Map<String, Double>,
        bikeByYears: Map<String, Double>,
        inLineSkateByYears: Map<String, Double>,
        hikeByYears: Map<String, Double>,
        barMode: BarMode,
        row: Int = 1,
        width: Int = 6
    ) {
        plot(row = row, width = width) {
            traces(
                buildBarByType(runByYears, Run),
                buildBarByType(bikeByYears, Ride),
                buildBarByType(inLineSkateByYears, InlineSkate),
                buildBarByType(hikeByYears, Hike)
            )

            layout {
                barmode = barMode
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
        runByYears: Map<String, Double>,
        bikeByYears: Map<String, Double>,
        inLineSkateByYears: Map<String, Double>,
        hikeByYears: Map<String, Double>,
        row: Int = 2,
        width: Int = 12
    ) {
        val annotationsList = mutableListOf<Text>()

        val cumulativeRun = ActivityHelper.cumulativeValue(runByYears)
        val cumulativeRide = ActivityHelper.cumulativeValue(bikeByYears)
        val cumulativeInlineSkate = ActivityHelper.cumulativeValue(inLineSkateByYears)
        val cumulativeHike = ActivityHelper.cumulativeValue(hikeByYears)

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
        activitiesByYear: Map<String, List<Activity>>,
        activityType: String,
        row: Int = 3,
        width: Int = 12
    ) {

        val annotationsList = mutableListOf<Text>()


        plot(row = row, width = width) {
            for (year in 2010..LocalDate.now().year) {
                val activities = if (activitiesByYear[year.toString()] != null) {
                    activitiesByYear[year.toString()]?.filter { activity -> activity.type == activityType }!!
                } else {
                    continue
                }
                val activitiesByDay = ActivityHelper.groupActivitiesByDay(activities, year)
                val cumulativeDistance = ActivityHelper.cumulativeDistance(activitiesByDay)

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
        activitiesByYear: Map<String, List<Activity>>,
        activityType: String,
        row: Int = 5,
        width: Int = 12
    ) {
        val annotationsList = mutableListOf<Text>()

        plot(row = row, width = width) {
            for (year in 2010..LocalDate.now().year) {
                val activities = if (activitiesByYear[year.toString()] != null) {
                    activitiesByYear[year.toString()]?.filter { activity -> activity.type == activityType }!!
                } else {
                    continue
                }
                val activitiesByDay = ActivityHelper.groupActivitiesByDay(activities, year)
                val cumulativeElevation = ActivityHelper.cumulativeElevation(activitiesByDay)

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