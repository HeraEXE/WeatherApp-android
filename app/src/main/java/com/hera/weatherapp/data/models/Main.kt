package com.hera.weatherapp.data.models

data class Main(
    val temp: Double,
    val pressure: Int,
    val humidity: Int,
    val temp_min: Double,
    val temp_max: Double
) {
    val tempK get() = "$temp K"
    val tempMinK get() = "$temp_min K"
    val tempMaxK get() = "$temp_max K"

    val tempF get() = String.format("%.2f", (temp - 273.0) * 1.8 + 32)+" °F"
    val tempMinF get() = String.format("%.2f", (temp_min - 273.0) * 1.8 + 32)+" °F"
    val tempMaxF get() = String.format("%.2f", (temp_max - 273.0) * 1.8 + 32)+" °F"

    val tempC get() = String.format("%.2f", temp - 273.0)+" °C"
    val tempMinC get() = String.format("%.2f", temp_min - 273.0)+" °C"
    val tempMaxC get() = String.format("%.2f", temp_max - 273.0)+" °C"

    val humidityString get() = "$humidity%"

}
