package me.nicolas.stravastats.ihm

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.chart.XYChart
import javafx.scene.control.Hyperlink
import javafx.scene.image.ImageView
import me.nicolas.stravastats.business.*
import me.nicolas.stravastats.business.badges.Badge
import me.nicolas.stravastats.business.badges.DistanceBadge
import me.nicolas.stravastats.business.badges.ElevationBadge
import me.nicolas.stravastats.business.badges.MovingTimeBadge
import me.nicolas.stravastats.service.*
import me.nicolas.stravastats.service.statistics.ActivityStatistic
import me.nicolas.stravastats.service.statistics.Statistic
import me.nicolas.stravastats.service.statistics.calculateBestElevationForDistance
import me.nicolas.stravastats.service.statistics.calculateBestTimeForDistance
import me.nicolas.stravastats.utils.GenericCache
import me.nicolas.stravastats.utils.SoftCache
import me.nicolas.stravastats.utils.formatDate
import tornadofx.Controller
import kotlin.collections.component1
import kotlin.collections.component2


class MainController(private val clientId: String, private val activities: ObservableList<Activity>) : Controller() {

    private val statsService = StatisticsService()

    private val chartsService = ChartsService()

    private val csvService = CSVService()

    private val badgesService = BadgesService()

    private val filteredActivitiesCache: GenericCache<String, List<Activity>> = SoftCache()

    private val famousClimbClimbBadgesCache: GenericCache<String, List<List<BadgeDisplay>>> = SoftCache()


    fun generateCSV(year: Int?) {

        val activitiesForYear: List<Activity> = if (year != null) {
            activities.groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            }[year.toString()] ?: emptyList()
        } else {
            activities
        }

        csvService.exportCSV(clientId, activitiesForYear, (year?.toString() ?: ""))
    }

    fun generateCharts(year: Int?) {
        chartsService.buildCharts(activities, year)
    }

    fun getFilteredActivities(activityType: String, year: Int?): List<Activity> {
        val filteredActivities = filteredActivitiesCache["$activityType-$year"] ?: this.activities
            .filterActivitiesByType(activityType)
            .filterActivitiesByYear(year)
        filteredActivitiesCache["$activityType-$year"] = filteredActivities

        return filteredActivities
    }


    fun getActiveDaysByActivityTypeByYear(activityType: String, year: Int?): Map<String, Int> {

        val filteredActivities = getFilteredActivities(activityType, year)

        return filteredActivities
            .groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance / 1000 } }
            .mapValues { entry -> entry.value.toInt() }
            .toMap()
    }

    fun getActiveDaysByActivityType(activityType: String): Map<String, Int> {

        val filteredActivities = this.activities
            .filterActivitiesByType(activityType)

        return filteredActivities
            .groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance / 1000 } }
            .mapValues { entry -> entry.value.toInt() }
            .toMap()
    }

    fun getActivitiesByYear(activityType: String): Map<String, List<Activity>> {

        val filteredActivities = this.activities
            .filterActivitiesByType(activityType)

        return ActivityHelper.groupActivitiesByYear(filteredActivities)
    }

    fun getActivitiesToDisplay(activityType: String, year: Int?): ObservableList<ActivityDisplay> {

        val filteredActivities = getFilteredActivities(activityType, year)

        return buildActivitiesToDisplay(filteredActivities)
    }

    fun getStatisticsToDisplay(activityType: String, year: Int?): ObservableList<StatisticDisplay> {

        val filteredActivities = getFilteredActivities(activityType, year)

        val statistics = when (activityType) {
            Ride -> statsService.computeRideStatistics(filteredActivities)
            Commute -> statsService.computeCommuteStatistics(filteredActivities)
            Run -> statsService.computeRunStatistics(filteredActivities)
            InlineSkate -> statsService.computeInlineSkateStatistics(filteredActivities)
            Hike -> statsService.computeHikeStatistics(filteredActivities)
            AlpineSki -> statsService.computeAlpineSkiStatistics(filteredActivities)

            else -> emptyList()
        }

        return buildStatisticsToDisplay(statistics)
    }


    fun getGeneralBadgesSetToDisplay(activityType: String, year: Int?): List<List<BadgeDisplay>> {
        val filteredActivities = getFilteredActivities(activityType, year)

        val badgesSets = mutableListOf<List<BadgeDisplay>>()
        when (activityType) {
            Ride -> {
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = DistanceBadge.rideBadgeSet.check(filteredActivities),
                        imageViewUrl = "images/racing.png"
                    )
                )
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = ElevationBadge.rideBadgeSet.check(filteredActivities),
                        imageViewUrl = "images/cycling.png"
                    )
                )
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = MovingTimeBadge.movingTimeBadgesSet.check(filteredActivities),
                        imageViewUrl = "images/stopwatch.png"
                    )
                )
            }
            Run -> {
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = DistanceBadge.runBadgeSet.check(filteredActivities),
                        imageViewUrl = "images/run.png"
                    )
                )
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = ElevationBadge.runBadgeSet.check(filteredActivities),
                        imageViewUrl = "images/run.png"
                    )
                )
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = MovingTimeBadge.movingTimeBadgesSet.check(filteredActivities),
                        imageViewUrl = "images/stopwatch.png"
                    )
                )
            }
        }
        return badgesSets
    }

    fun getFamousClimbBadgesSetToDisplay(activityType: String, year: Int?): List<List<BadgeDisplay>> {
        val famousClimbClimbBadges =
            famousClimbClimbBadgesCache["$activityType-$year"] ?: buildFamousClimbBadgesSet(activityType, year)
        famousClimbClimbBadgesCache["$activityType-$year"] = famousClimbClimbBadges

        return famousClimbClimbBadges
    }

    private fun buildFamousClimbBadgesSet(activityType: String, year: Int?): List<List<BadgeDisplay>> {
        val filteredActivities = getFilteredActivities(activityType, year)

        val badgesSets = mutableListOf<List<BadgeDisplay>>()
        when (activityType) {
            Ride -> {
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = badgesService.getAlpesFamousBadges(filteredActivities),
                        imageViewUrl = "images/cycling.png"
                    )
                )
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = badgesService.getPyreneesFamousBadges(filteredActivities),
                        imageViewUrl = "images/cycling.png"
                    )
                )
            }
        }

        return badgesSets
    }

    fun buildDistanceByMonthsSeries(activityType: String, year: Int): ObservableList<XYChart.Data<String, Number>> {

        val filteredActivities = getFilteredActivities(activityType, year)

        val activitiesByMonth: Map<String, List<Activity>> = ActivityHelper.groupActivitiesByMonth(filteredActivities)
        val distanceByMonth: Map<String, Double> = activitiesByMonth.mapValues { (_, activities) ->
            activities.sumOf { activity ->
                activity.distance / 1000
            }
        }

        return FXCollections.observableList(distanceByMonth.map { entry ->
            XYChart.Data(entry.key, entry.value)
        })
    }

    fun buildDistanceByWeeksSeries(activityType: String, year: Int): ObservableList<XYChart.Data<String, Number>> {

        val filteredActivities = getFilteredActivities(activityType, year)

        val activitiesByWeek: Map<String, List<Activity>> = ActivityHelper.groupActivitiesByWeek(filteredActivities)
        val distanceByWeek: Map<String, Double> = activitiesByWeek.mapValues { (_, activities) ->
            activities.sumOf { activity ->
                activity.distance / 1000
            }
        }

        return FXCollections.observableList(distanceByWeek.map { entry ->
            XYChart.Data(entry.key, entry.value)
        })
    }

    fun buildDistanceByDaysSeries(activityType: String, year: Int): ObservableList<XYChart.Data<String, Number>>? {

        val filteredActivities = getFilteredActivities(activityType, year)

        val activitiesByDay: Map<String, List<Activity>> = ActivityHelper.groupActivitiesByDay(filteredActivities, year)
        val distanceByDay: Map<String, Double> = activitiesByDay.mapValues { (_, activities) ->
            activities.sumOf { activity -> activity.distance / 1000 }
        }

        return FXCollections.observableList(distanceByDay.map { entry ->
            XYChart.Data(entry.key, entry.value)
        })
    }

    private fun List<Activity>.filterActivitiesByType(activityType: String): List<Activity> {
        return if (activityType == Commute) {
            this.filter { activity -> activity.type == Ride && activity.commute }
        } else {
            this.filter { activity -> activity.type == activityType && !activity.commute }
        }
    }

    private fun List<Activity>.filterActivitiesByYear(year: Int?): List<Activity> {
        return if (year == null) {
            this
        } else {
            this.filter { activity -> activity.startDateLocal.subSequence(0, 4).toString().toInt() == year }
        }
    }

    private fun buildBadgesToDisplay(
        badgesList: List<Triple<Badge, Activity?, Boolean>>,
        imageViewUrl: String
    ): List<BadgeDisplay> {

        val badges = badgesList.map { triple ->
            val isCompleted = triple.third
            val activity = triple.second
            val badge = triple.first

            val imageView = ImageView(imageViewUrl)
                .apply {
                    if (!isCompleted) {
                        opacity = 0.15
                    }
                    fitWidth = 110.0
                    isPreserveRatio = true
                    isSmooth = true
                    isCache = true
                }

            val hyperlink = Hyperlink("", imageView)
                .apply {
                    onAction = if (triple.second != null) {
                        EventHandler {
                            if (activity != null) {
                                ActivityDetailView(activity).openModal()
                            }
                        }
                    } else {
                        EventHandler {
                        }
                    }
                }

            BadgeDisplay(badge.toString(), hyperlink, badge)
        }

        return badges
    }

    private fun buildStatisticsToDisplay(statistics: List<Statistic>): ObservableList<StatisticDisplay> {

        val activityStatistics = statistics.map { statistic ->
            when (statistic) {
                is ActivityStatistic -> {
                    val hyperlink = if (statistic.activity != null) {
                        Hyperlink(statistic.activity.toString()).apply {
                            onAction = EventHandler {
                                ActivityDetailView(statistic.activity!!).openModal()
                            }
                        }
                    } else {
                        null
                    }

                    StatisticDisplay(statistic.name, statistic.value, hyperlink)
                }
                else -> StatisticDisplay(statistic.name, statistic.toString(), null)
            }
        }
        return FXCollections.observableArrayList(activityStatistics)
    }

    private fun buildActivitiesToDisplay(activities: List<Activity>): ObservableList<ActivityDisplay> {

        val activitiesToDisplay = activities.map { activity ->

            val hyperlink = Hyperlink(activity.name).apply {
                onAction = EventHandler {
                    ActivityDetailView(activity).openModal()
                }
            }

            ActivityDisplay(
                hyperlink,
                activity.distance,
                activity.elapsedTime,
                activity.totalElevationGain,
                activity.calculateTotalDescentGain(),
                activity.averageSpeed,
                activity.calculateBestTimeForDistance(1000.0)?.getFormattedSpeed() ?: "",
                activity.calculateBestElevationForDistance(250.0)?.getFormattedGradient() ?: "",
                activity.calculateBestElevationForDistance(500.0)?.getFormattedGradient() ?: "",
                activity.startDateLocal.formatDate()
            )
        }
        return FXCollections.observableArrayList(activitiesToDisplay)
    }
}
