package com.hera.weatherapp.data.models

data class Sys(
        val type: Int,
        val id: Long,
        val message: Double,
        val country: String,
        val sunrise: Long,
        val sunset: Long
)
