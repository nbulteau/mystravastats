package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonProperty
import me.nicolas.stravastats.utils.formatSeconds

data class SegmentEffort(
    val achievements: List<Achievement>,
    val activity: MetaActivity,
    val athlete: MetaAthlete,
    @JsonProperty("average_cadence")
    val averageCadence: Double,
    @JsonProperty("average_heartrate")
    val averageHeartRate: Double,
    @JsonProperty("average_watts")
    val averageWatts: Double,
    @JsonProperty("device_watts")
    val deviceWatts: Boolean,
    val distance: Double,
    @JsonProperty("elapsed_time")
    val elapsedTime: Int,
    @JsonProperty("end_index")
    val endIndex: Int,
    val hidden: Boolean,
    val id: Long,
    @JsonProperty("kom_rank")
    val komRank: Int?,
    @JsonProperty("max_heartrate")
    val maxHeartRate: Double,
    @JsonProperty("moving_time")
    val movingTime: Int,
    val name: String,
    @JsonProperty("pr_rank")
    val prRank: Int?,
    @JsonProperty("resource_state")
    val resourceState: Int,
    val segment: Segment,
    @JsonProperty("start_date")
    val startDate: String,
    @JsonProperty("start_date_local")
    val startDateLocal: String,
    @JsonProperty("start_index")
    val startIndex: Int,
    val visibility: String
) {
    fun getFormattedSpeed(type: String): String {
        return if (type == Run) {
            "${(elapsedTime * 1000 / distance).formatSeconds()}/km"
        } else {
            "%.02f km/h".format(distance / elapsedTime * 3600 / 1000)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SegmentEffort

        if (endIndex != other.endIndex) return false
        if (name != other.name) return false
        return startIndex == other.startIndex
    }

    override fun hashCode(): Int {
        var result = endIndex
        result = 31 * result + name.hashCode()
        result = 31 * result + startIndex
        return result
    }


}