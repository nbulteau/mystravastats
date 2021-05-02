package me.nicolas.stravastats.core.charts

import kscience.plotly.models.Bar
import kscience.plotly.models.LineShape
import kscience.plotly.models.Scatter
import me.nicolas.stravastats.business.Activity
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
            line.shape= LineShape.spline
            connectgaps=true
            name = type
        }

        fun buildLineByYear(activities: Map<String, Double>, year: Int) = Scatter {
            x.set(activities.keys)
            y.set(activities.values)
            line.shape= LineShape.spline
            connectgaps=true
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
                    .map { activity -> activity.averageSpeed * 3.6}
                    .average()
            }

    }

    abstract fun build()
}