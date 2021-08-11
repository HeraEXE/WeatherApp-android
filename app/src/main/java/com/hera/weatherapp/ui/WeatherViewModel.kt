package com.hera.weatherapp.ui

import androidx.lifecycle.ViewModel
import com.hera.weatherapp.data.models.CurrentWeatherResponse
import com.hera.weatherapp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val query = MutableStateFlow("")
    var currentWeather: CurrentWeatherResponse? = null
    val weatherFlow = query.flatMapLatest { q ->
        repository.getCurrentWeather(q)
    }

    var degreeUnit = "KELVIN"
    var speedUnit = "kmph"


    fun changeDegreeUnit() {
        degreeUnit = when (degreeUnit) {
            "KELVIN" -> "CELSIUS"
            "CELSIUS" -> "FAHRENHEIT"
            "FAHRENHEIT" -> "KELVIN"
            else -> "KELVIN"
        }
    }


    fun changeSpeedUnit() {
        speedUnit = when(speedUnit) {
            "kmph" -> "mph"
            "mph" -> "kmph"
            else -> "kmph"
        }
    }
}