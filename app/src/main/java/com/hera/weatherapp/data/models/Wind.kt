package com.hera.weatherapp.data.models

import kotlin.math.roundToInt

data class Wind(
        val speed: Double,
        val deg: Int
) {
    val speedKmpH get() = "${speed.roundToInt()} km/h"
    val speedMpH get() = "${convertKilometersToMiles(speed).roundToInt()} mph"

    private fun convertKilometersToMiles(speed: Double) =
            speed / 1.609
}
