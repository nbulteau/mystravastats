package me.nicolas.stravastats.business

abstract class Statistic(
    val name: String,
    protected val activities: List<Activity>
) {
    fun displayName(): String {
        return "${name.padEnd(30)} : "
    }

    abstract fun display(): String
}

internal class GlobalStatistic(
    name: String,
    activities: List<Activity>,
    private val formatString: String,
    private val function: (List<Activity>) -> Number
) : Statistic(name, activities) {

    override fun display(): String {
        return formatString.format(function(activities))
    }
}

abstract class ActivityStatistic(
    name: String,
    activities: List<Activity>
) : Statistic(name, activities) {

    protected var activity: Activity? = null
}



