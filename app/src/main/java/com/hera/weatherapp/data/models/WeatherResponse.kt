package com.hera.weatherapp.data.models

data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main,
    val visibility: Int,
    val name: String
)
