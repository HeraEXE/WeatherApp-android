package com.hera.weatherapp.data.models

data class CurrentWeatherResponse(
        val coord: Coord,
        val weather: List<Weather>,
        val base: String,
        val main: Main,
        val visibility: Int,
        val wind: Wind,
        val clouds: Clouds,
        val dt: Long,
        val sys: Sys,
        val timezone: Long,
        val id: Long,
        val name: String,
        val cod: Int
)


data class Coord(
        val lon: Double,
        val lat: Double
)


data class Weather(
        val id: Long,
        val main: String,
        val description: String,
        val icon: String
)


data class Clouds(
        val all: Int
)


data class Sys(
        val type: Int,
        val id: Long,
        val message: Double,
        val country: String,
        val sunrise: Long,
        val sunset: Long
)
