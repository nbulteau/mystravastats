package me.nicolas.stravastats.service.charts

import me.nicolas.stravastats.MyStravaStatsApp
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.statistics.EddingtonStatistic
import space.kscience.plotly.PlotGrid
import space.kscience.plotly.PlotlyPage
import space.kscience.plotly.UnstablePlotlyAPI
import space.kscience.plotly.layout
import space.kscience.plotly.models.*
import java.nio.file.Files
import kotlin.math.abs

@Suppress("DEPRECATION")
@UnstablePlotlyAPI
abstract class Chart {

    companion object {
        fun buildBarByType(activities: Map<String, Double>, type: String) = Bar {
            x.set(activities.keys)
            y.set(activities.values)
            name = type
        }

        fun buildLineByType(activities: Map<String, Double>, type: String) = Scatter {
            x.set(activities.keys)
            y.set(activities.values)
            line.shape = LineShape.spline
            connectgaps = true
            name = type
        }

        fun buildLineByYear(activities: Map<String, Double>, year: Int) = Scatter {
            x.set(activities.keys)
            y.set(activities.values)
            line.shape = LineShape.spline
            connectgaps = true
            name = "$year"
        }
    }

    fun renderAndOpenBrowser(plot: PlotlyPage) {
        val actualFile = Files.createTempFile("tempPlot", ".html")
        Files.createDirectories(actualFile.parent)
        Files.writeString(actualFile, plot.render())

        MyStravaStatsApp.openBrowser(actualFile.toString())
    }

    fun PlotGrid.buildEddingtonNumberPlotByType(
        activities: List<Activity>,
        activityType: String,
        row: Int = 6,
        width: Int = 6
    ) {
        val eddingtonStatistic = EddingtonStatistic(activities.filter { activity -> activity.type == activityType })
        val eddingtonNumber = eddingtonStatistic.eddingtonNumber

        val eddingtonBar = Bar {
            x.set(listOf(eddingtonNumber))
            y.set(listOf(eddingtonNumber))
            showlegend = false
        }

        val nbDaysDistanceIsReached = eddingtonStatistic.nbDaysDistanceIsReached

        val eddingtonScatter = Scatter {
            x.set((0..nbDaysDistanceIsReached.size).toList())
            y.set((0..nbDaysDistanceIsReached.size).toList())
            val stringsBefore =
                (0 until (eddingtonNumber + 1).coerceAtMost(nbDaysDistanceIsReached.size - 1)).map { i -> "On ${nbDaysDistanceIsReached[i] + 1} days you covered at least $i km." }
                    .toList()
            val stringsAfter =
                (eddingtonNumber - 1 until nbDaysDistanceIsReached.size - 1).map { i ->
                    "On ${nbDaysDistanceIsReached[i] + 1} days you covered at least ${i + 1} km." +
                            "You need ${abs(i - nbDaysDistanceIsReached[i] + 1)} more days (of ${i + 1} km or more) to achieve an Eddington number of ${i + 1}"
                }
                    .toList()
            text(*(stringsBefore + stringsAfter).toTypedArray())

            name = "Eddington"
            line.shape = LineShape.linear
            marker {
                color("Orange")
            }
        }

        plot(row = row, width = width) {
            traces(
                Bar {
                    x.set((1..nbDaysDistanceIsReached.size).toList())
                    y.set(nbDaysDistanceIsReached)
                    name = "Times completed"
                    hoverinfo = "skip"
                },
                eddingtonBar,
                eddingtonScatter
            )

            layout {
                barmode = BarMode.overlay
                title = "$activityType Eddington number : $eddingtonNumber km"

                xaxis {
                    title = "Km"
                    type = AxisType.linear
                    range(0.0.rangeTo(nbDaysDistanceIsReached.size.toDouble()))
                }

                yaxis {
                    type = AxisType.linear
                }

                legend {
                    bgcolor("#E2E2E2")
                    traceorder = TraceOrder.normal
                }

                // not yet implemented with plotlykt 0.5.0
                /*
                annotation {
                    xref = "x"
                    yref = "y"
                    position(eddingtonNumber, eddingtonNumber)
                    text = "Eddington number : $eddingtonNumber"
                    font {
                        family = "Arial"
                        size = 12
                        color("black")
                    }
                    showarrow = true
                }
                */
            }
        }
    }

    abstract fun build()
}