package me.nicolas.stravastats.core.charts

import kscience.plotly.*
import kscience.plotly.models.*
import me.nicolas.stravastats.business.*
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.*

internal class KilometersByMonthsChart {

    companion object {

        fun buildCharts(activities: List<Activity>, year: Int) {

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

            val activitiesByDay = getActivitiesByDay(activities, year)

            val runByDays = activitiesByDay.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Run }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val bikeByDays = activitiesByDay.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Ride }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val inLineSkateByDays = activitiesByDay.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == InlineSkate }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }
            val hikeByDays = activitiesByDay.mapValues { (_, activities) ->
                activities
                    .filter { activity -> activity.type == Hike }
                    .sumByDouble { activity -> activity.distance / 1000 }
            }


            val plot = Plotly.grid {
                buildBarModeGroupPlot(runByMonths, bikeByMonths, inLineSkateByMonths, hikeByMonths, year)
                buildCumulativePlot(runByMonths, bikeByMonths, inLineSkateByMonths, hikeByMonths, year)
                buildBarModeStackByDayPlot(runByDays, bikeByDays, inLineSkateByDays, hikeByDays, year)
            }

            plot.makeFile()
        }

        private fun PlotGrid.buildBarModeGroupPlot(
            runByMonths: Map<String, Double>,
            bikeByMonths: Map<String, Double>,
            inLineSkateByMonths: Map<String, Double>,
            hikeByMonths: Map<String, Double>,
            year: Int
        ) {
            plot(row = 1, width = 6) {
                traces(
                    buildBarByMonth(runByMonths, Run),
                    buildBarByMonth(bikeByMonths, Ride),
                    buildBarByMonth(inLineSkateByMonths, InlineSkate),
                    buildBarByMonth(hikeByMonths, Hike)
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
                .groupBy { activity -> activity.startDateLocal.subSequence(5, 7).toString() }
                .toMutableMap()

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
            year: Int
        ) {
            plot(row = 1, width = 6) {
                traces(
                    buildLine(getCumulativeSum(runByMonths), Run),
                    buildLine(getCumulativeSum(bikeByMonths), Ride),
                    buildLine(getCumulativeSum(inLineSkateByMonths), InlineSkate),
                    buildLine(getCumulativeSum(hikeByMonths), Hike)
                )

                layout {
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

        private fun getCumulativeSum(activities: Map<String, Double>): Map<String, Double> {
            var sum = 0.0
            return activities.mapValues { (_, value) -> sum += value; sum }
        }

        private fun buildBarByMonth(activitiesByMonths: Map<String, Double>, type: String): Bar {
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

        private fun PlotGrid.buildBarModeStackByDayPlot(
            runByDays: Map<String, Double>,
            bikeByDays: Map<String, Double>,
            inLineSkateByDays: Map<String, Double>,
            hikeByDays: Map<String, Double>,
            year: Int
        ) {
            plot(row = 2, width = 12) {
                traces(
                    buildBar(runByDays, Run),
                    buildBar(bikeByDays, Ride),
                    buildBar(inLineSkateByDays, InlineSkate),
                    buildBar(hikeByDays, Hike)
                )

                layout {
                    barmode = BarMode.stack
                    title = "kilometers by day for $year"

                    xaxis {
                        title = "Day"
                        type = AxisType.category
                    }
                    yaxis {
                        title = "Km"
                    }
                    legend {
                        xanchor = XAnchor.right
                        bgcolor("#E2E2E2")
                        traceorder = TraceOrder.normal
                    }
                }
            }
        }

        private fun getActivitiesByDay(activities: List<Activity>, year: Int): SortedMap<String, List<Activity>> {
            val activitiesGroupedByDay = activities
                .groupBy { activity -> activity.startDateLocal.subSequence(5, 10).toString() }

            val activitiesByDay = activitiesGroupedByDay.toMutableMap()
            // init current date to first of the year
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

        private fun buildBar(activitiesByDays: Map<String, Double>, type: String) =
            Bar {
                x.set(activitiesByDays.keys)
                y.set(activitiesByDays.values)
                name = type
            }
    }
}
