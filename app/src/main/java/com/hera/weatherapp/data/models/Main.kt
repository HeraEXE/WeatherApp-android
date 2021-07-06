package com.hera.weatherapp.data.models

import kotlin.math.roundToInt

data class Main(
    val temp: Double,
    val pressure: Int,
    val humidity: Int,
    val temp_min: Double,
    val temp_max: Double
) {
    val tempK get() = "${temp.roundToInt()} K"
    val tempMinK get() = "${temp_min.roundToInt()} K"
    val tempMaxK get() = "${temp_max.roundToInt()} K"

    val tempF get() = "${convertKelvinToFahrenheit(temp).roundToInt()} °F"
    val tempMinF get() = "${convertKelvinToFahrenheit(temp_min).roundToInt()} °F"
    val tempMaxF get() = "${convertKelvinToFahrenheit(temp_max).roundToInt()} °F"

    val tempC get() = "${convertKelvinToCelsius(temp).roundToInt()} °C"
    val tempMinC get() = "${convertKelvinToCelsius(temp_min).roundToInt()} °C"
    val tempMaxC get() = "${convertKelvinToCelsius(temp_max).roundToInt()} °C"

    val humidityString get() = "$humidity%"


    private fun convertKelvinToFahrenheit(temp: Double) =
            (temp - 273.0) * 1.8 + 32

    private fun convertKelvinToCelsius(temp: Double) =
            temp - 273.0
}
