package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.GeoCoordinate

data class LocationBadge(
    override val label: String,
    val elevation: Int,
    val geoCoordinate: GeoCoordinate,
    val location: String
) : Badge(label) {

    override fun check(activities: List<Activity>): Pair<Activity?, Boolean> {
        for (activity in activities) {
            // filter the activities that start more than 100 kms away
            if (this.geoCoordinate.haversineInKM(activity.startLatitude, activity.startLongitude) < 100) {
                for (coords in activity.stream?.latitudeLongitude?.data!!) {
                    if (this.match(coords[0], coords[1])) {
                        return Pair(activity, true)
                    }
                }
            }
        }
        return Pair(null, false)
    }

    override fun toString() = "$label\n$elevation m"

    // match if distance from the geo localisation is less than 200 m
    private fun match(latitude: Double, longitude: Double) = geoCoordinate.haversineInM(latitude, longitude) < 200

    companion object {
        val COL_AGNEL = LocationBadge(
            label = "Col Agnel",
            elevation = 2740,
            geoCoordinate = GeoCoordinate(44.6839194, 6.9795741),
            location = "Alpes"
        )
        val CIME_DE_LA_BONETTE = LocationBadge(
            label = "Cime de la Bonette",
            elevation = 2802,
            geoCoordinate = GeoCoordinate(44.3216186, 6.8068886),
            location = "Alpes"
        )
        val COL_DE_LA_CAYOLLE = LocationBadge(
            label = "Col de la Cayolle",
            elevation = 2324,
            geoCoordinate = GeoCoordinate(44.259142, 6.7439734),
            location = "Alpes"
        )
        val COL_D_IZOARD = LocationBadge(
            label = "Col d'Izoard",
            elevation = 2362,
            geoCoordinate = GeoCoordinate(44.8200267, 6.7350408),
            location = "Alpes"
        )
        val COL_DE_VARS = LocationBadge(
            label = "Col de Vars",
            elevation = 2108,
            geoCoordinate = GeoCoordinate(44.5387261, 6.7028698),
            location = "Alpes"
        )
        val COL_DU_GALIBIER = LocationBadge(
            label = "Col du Galibier",
            elevation = 2642,
            geoCoordinate = GeoCoordinate(45.0641651, 6.407878),
            location = "Alpes"
        )
        val RISOUL = LocationBadge(
            label = "Risoul",
            elevation = 1850,
            geoCoordinate = GeoCoordinate(44.6491889, 6.6387088),
            location = "Alpes"
        )
        val VALBERG = LocationBadge(
            label = "Valberg",
            elevation = 1673,
            geoCoordinate = GeoCoordinate(44.0956228, 6.9301384),
            location = "Alpes"
        )
        val ALPE_D_HUEZ = LocationBadge(
            label = "Alpe d'Huez",
            elevation = 1850,
            geoCoordinate = GeoCoordinate(45.092401, 6.0699443),
            location = "Alpes"
        )
        val ISOLA_2000 = LocationBadge(
            label = "Isola 2000",
            elevation = 2000,
            geoCoordinate = GeoCoordinate(44.186683, 7.157875),
            location = "Alpes"
        )
        val COL_DE_L_ISERAN = LocationBadge(
            label = "Col de l'Iseran",
            elevation = 2764,
            geoCoordinate = GeoCoordinate(45.4171195, 7.0308387),
            location = "Alpes"
        )
        val COL_DU_MONT_CENIS = LocationBadge(
            label = "Col du Mont-Cenis",
            elevation = 2085,
            geoCoordinate = GeoCoordinate(45.2598281, 6.9008841),
            location = "Alpes"
        )
        val COL_DU_TELEGRAPHE = LocationBadge(
            label = "Col du Télégraphe",
            elevation = 1566,
            geoCoordinate = GeoCoordinate(45.2026999, 6.4446143),
            location = "Alpes"
        )
        val COL_DE_LA_LOZE = LocationBadge(
            label = "Col de la Loze",
            elevation = 2275,
            geoCoordinate = GeoCoordinate(45.407564, 6.602688),
            location = "Alpes"
        )
        val COL_DES_CHAMPS = LocationBadge(
            label = "Col des Champs",
            elevation = 2045,
            geoCoordinate = GeoCoordinate(44.175062, 6.697894),
            location = "Alpes"
        )
        val MUR_DE_BRETAGNE = LocationBadge(
            label = "Mur de Bretagne",
            elevation = 293,
            geoCoordinate = GeoCoordinate(48.228680749433366, -2.9981557314865346),
            location = "Massif armoricain"
        )
        val COL_DU_TOURMALET = LocationBadge(
            label = "Col du Tourmalet",
            elevation = 2115,
            geoCoordinate = GeoCoordinate(42.9083885, 0.1452852),
            location = "Pyrénées"
        )
        val cyclingBadges =
            listOf(
                COL_AGNEL,
                COL_D_IZOARD,
                COL_DE_VARS,
                COL_DU_GALIBIER,
                RISOUL,
                VALBERG,
                ISOLA_2000,
                ALPE_D_HUEZ,
                CIME_DE_LA_BONETTE,
                COL_DE_LA_CAYOLLE,
                COL_DE_L_ISERAN,
                COL_DU_MONT_CENIS,
                COL_DU_TELEGRAPHE,
                COL_DE_LA_LOZE,
                COL_DES_CHAMPS,
                COL_DU_TOURMALET,
                MUR_DE_BRETAGNE
            )
    }
}