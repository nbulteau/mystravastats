package me.nicolas.stravastats.service.charts

import me.nicolas.stravastats.business.Activity
import space.kscience.plotly.PlotGrid
import space.kscience.plotly.UnstablePlotlyAPI
import space.kscience.plotly.layout
import space.kscience.plotly.models.*
import kotlin.math.abs

@OptIn(UnstablePlotlyAPI::class)
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

    fun PlotGrid.buildEddingtonNumberPlotByType(
        row: Int,
        width: Int,
        activities: List<Activity>,
        activityType: String
    ) {

        val activeDaysList: Map<String, Int> = activities
            .filter { activity -> activity.type == activityType }
            .groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance / 1000 } }
            .mapValues { entry -> entry.value.toInt() }
            .toMap()

        if (activeDaysList.isEmpty()) {
            return // No Plot
        }

        // counts = number of time we reach a distance
        val counts: MutableList<Int> = // init to 0
            activeDaysList.maxOf { entry -> entry.value }.let { List(it) { 0 }.toMutableList() }

        var eddingtonNumber = 0
        activeDaysList.forEach { entry: Map.Entry<String, Int> ->
            for (day in entry.value downTo 1) {
                counts[day - 1] += 1
            }
        }

        for (day in counts.size downTo 1) {
            if (counts[day - 1] >= day) {
                eddingtonNumber = day
                break
            }
        }

        val eddingtonBar = Bar {
            x.set(listOf(eddingtonNumber))
            y.set(listOf(eddingtonNumber))
            showlegend = false
        }

        val eddingtonScatter = Scatter {
            x.set((0..counts.size).toList())
            y.set((0..counts.size).toList())
            val stringsBefore =
                (0 until (eddingtonNumber + 1).coerceAtMost(counts.size - 1)).map { i -> "On ${counts[i] + 1} days you covered at least $i km." }
                    .toList()
            val stringsAfter =
                (eddingtonNumber - 1 until counts.size - 1).map { i ->
                    "On ${counts[i] + 1} days you covered at least ${i + 1} km." +
                            "You need ${abs(i - counts[i] + 1)} more days (of ${i + 1} km or more) to achieve an Eddington number of ${i + 1}"
                }
                    .toList()
            text(*(stringsBefore + stringsAfter).toTypedArray())

            name = "Eddington"
            line.shape = LineShape.linear
            marker {
                color("Orange")
            }
        }

        val eddingtonText = Text {
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

        plot(row = row, width = width) {
            traces(
                Bar {
                    x.set((1..counts.size).toList())
                    y.set(counts)
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
                    range = 0.0.rangeTo(counts.size.toDouble())
                }

                yaxis {
                    type = AxisType.linear
                }

                legend {
                    bgcolor("#E2E2E2")
                    traceorder = TraceOrder.normal
                }
                annotation(eddingtonText)
            }
        }
    }

    abstract fun build()
}