package me.nicolas.stravastats.core.charts

import kotlinx.css.embed
import kscience.plotly.*
import kscience.plotly.Plotly.plot
import kscience.plotly.models.*
import me.nicolas.stravastats.business.*
import java.time.LocalDate
import java.util.*

internal class KilometersForAYearChart {

    companion object {

        fun buildKilometersForAYearCharts(activities: List<Activity>, year: Int) {

            val activitiesByDay = getActivitiesByDay(year, activities)

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

            val plot = buildBarModeStackPlot(runByDays, bikeByDays, inLineSkateByDays, hikeByDays, year)
            plot.makeFile(resourceLocation = ResourceLocation.EMBED)
        }

        private fun buildBarModeStackPlot(
            runByDays: Map<String, Double>,
            bikeByDays: Map<String, Double>,
            inLineSkateByDays: Map<String, Double>,
            hikeByDays: Map<String, Double>,
            year: Int
        ): Plot {
            return plot() {
                traces(
                    buildBar(runByDays, Run),
                    buildBar(bikeByDays, Ride),
                    buildBar(inLineSkateByDays, InlineSkate),
                    buildBar(hikeByDays, Hike)
                )

                layout {
                    barmode = BarMode.stack
                    title = "Cumulative kilometers by day for $year"

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
                }
            }
        }

        private fun getActivitiesByDay(year: Int, activities: List<Activity>): SortedMap<String, List<Activity>> {
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

        private fun buildBar(activitiesByDays: Map<String, Double>, type: String): Bar {

            return Bar {
                x.set(activitiesByDays.keys)
                y.set(activitiesByDays.values)
                name = type
            }
        }

        private fun buildLine(activitiesByDays: Map<String, Double>, type: String): Scatter {

            return Scatter {
                x.set(activitiesByDays.keys)
                y.set(activitiesByDays.values)
                name = type
            }
        }
    }
}