package me.nicolas.stravastats.core.charts

import io.javalin.plugin.openapi.annotations.toFormattedString
import space.kscience.plotly.PlotGrid
import space.kscience.plotly.layout
import space.kscience.plotly.models.*
import me.nicolas.stravastats.business.Activity
import space.kscience.dataforge.values.Value
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.*

abstract class Chart {

    companion object {

        fun groupActivitiesByYear(activities: List<Activity>): Map<String, List<Activity>> {
            val activitiesByYear = activities
                .groupBy { activity ->
                    activity.startDateLocal.subSequence(0, 4).toString()
                }.toMutableMap()

            // Add years without activities
            val min = activitiesByYear.keys.minOf { it.toInt() }
            val max = activitiesByYear.keys.maxOf { it.toInt() }
            for (year in min..max) {
                if (!activitiesByYear.contains("$year")) {
                    activitiesByYear["$year"] = emptyList()
                }
            }
            return activitiesByYear.toSortedMap()
        }

        fun groupActivitiesByMonth(activities: List<Activity>): Map<String, List<Activity>> {
            val activitiesByMonth = activities
                .groupBy { activity -> activity.startDateLocal.subSequence(5, 7).toString() }
                .toMutableMap()

            // Add months without activities
            for (month in (1..12)) {
                if (!activitiesByMonth.contains("$month".padStart(2, '0'))) {
                    activitiesByMonth["$month".padStart(2, '0')] = emptyList()
                }
            }

            return activitiesByMonth.toSortedMap().mapKeys { (key, _) ->
                Month.of(key.toInt()).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
            }.toMap()
        }

        fun groupActivitiesByDay(activities: List<Activity>, year: Int): Map<String, List<Activity>> {
            val activitiesByDay = activities
                .groupBy { activity -> activity.startDateLocal.subSequence(5, 10).toString() }
                .toMutableMap()

            // Add days without activities
            var currentDate = LocalDate.ofYearDay(year, 1)
            for (i in (0..365 + if (currentDate.isLeapYear) 1 else 0)) {
                currentDate = currentDate.plusDays(1L)
                val dayString =
                    "${currentDate.monthValue}".padStart(2, '0') + "-" + "${currentDate.dayOfMonth}".padStart(2, '0')
                if (!activitiesByDay.containsKey(dayString)) {
                    activitiesByDay[dayString] = emptyList()
                }
            }

            return activitiesByDay.toSortedMap()
        }

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

        fun cumulativeValue(activities: Map<String, Double>): Map<String, Double> {
            var sum = 0.0
            return activities.mapValues { (_, value) -> sum += value; sum }
        }

        fun cumulativeDistance(activities: Map<String, List<Activity>>): Map<String, Double> {
            var sum = 0.0
            return activities.mapValues { (_, activities) ->
                sum += activities.sumOf { activity -> activity.distance / 1000 }; sum
            }
        }

        fun cumulativeElevation(activities: Map<String, List<Activity>>): Map<String, Double> {
            var sum = 0.0
            return activities.mapValues { (_, activities) ->
                sum += activities.sumOf { activity -> activity.totalElevationGain }; sum
            }
        }

        fun sumDistanceByType(activities: Map<String, List<Activity>>, type: String) =
            activities.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == type }
                    .sumOf { activity -> activity.distance / 1000 }
            }

        fun sumElevationByType(activities: Map<String, List<Activity>>, type: String) =
            activities.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == type }
                    .sumOf { activity -> activity.totalElevationGain }
            }

        fun averageSpeedByType(activities: Map<String, List<Activity>>, type: String) =
            activities.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == type }
                    .map { activity -> activity.averageSpeed * 3.6 }
                    .average()
            }

    }

    fun PlotGrid.buildEddingtonNumberPlotByType(
        row: Int,
        width: Int,
        activities: List<Activity>,
        activityType: String
    ) {

        val activeDaysList = activities
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
                (0 until eddingtonNumber + 1).map { i -> "On ${counts[i]} days you covered at least $i km." }
                    .toList()
            val stringsAfter =
                (eddingtonNumber - 1 until counts.size).map { i -> "On ${counts[i]} days you covered at least $i km.\n\rYou need ${i - counts[i] + 2} more days (of ${i + 2} km or more) to achieve an Eddington number of ${i + 2}" }
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