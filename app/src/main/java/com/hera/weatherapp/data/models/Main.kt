package com.hera.weatherapp.data.models

import com.google.gson.annotations.SerializedName
import kotlin.math.roundToInt

data class Main(
        val temp: Double,
        @SerializedName("feels_like")
        val feelsLike: Double,
        @SerializedName("temp_min")
        val tempMin: Double,
        @SerializedName("temp_max")
        val tempMax: Double,
        val pressure: Int,
        val humidity: Int
) {
    val tempK get() = "${temp.roundToInt()} K"
    val tempFeelsLikeK get() = "${feelsLike.roundToInt()} K"
    val tempMinK get() = "${tempMin.roundToInt()} K"
    val tempMaxK get() = "${tempMax.roundToInt()} K"

    val tempF get() = "${convertKelvinToFahrenheit(temp).roundToInt()} °F"
    val tempFeelsLikeF get() = "${convertKelvinToFahrenheit(feelsLike).roundToInt()} °F"
    val tempMinF get() = "${convertKelvinToFahrenheit(tempMin).roundToInt()} °F"
    val tempMaxF get() = "${convertKelvinToFahrenheit(tempMax).roundToInt()} °F"

    val tempC get() = "${convertKelvinToCelsius(temp).roundToInt()} °C"
    val tempFeelsLikeC get() = "${convertKelvinToCelsius(feelsLike).roundToInt()} °C"
    val tempMinC get() = "${convertKelvinToCelsius(tempMin).roundToInt()} °C"
    val tempMaxC get() = "${convertKelvinToCelsius(tempMax).roundToInt()} °C"

    val humidityString get() = "$humidity%"


    private fun convertKelvinToFahrenheit(temp: Double) =
            (temp - 273.0) * 1.8 + 32

    private fun convertKelvinToCelsius(temp: Double) =
            temp - 273.0
}
