package com.hera.weatherapp.ui

import androidx.lifecycle.ViewModel
import com.hera.weatherapp.data.WeatherApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val api: WeatherApi
) : ViewModel() {

    val error = MutableStateFlow(0)
    val q = MutableStateFlow("")
    val degreeUnit = MutableStateFlow("KELVIN")
    val speedUnit = MutableStateFlow("kmph")
    val weather = combine(q, degreeUnit, speedUnit) { q, degreeUnit, speedUnit ->
        Triple(q, degreeUnit, speedUnit)
    }.flatMapLatest { (q, degreeUnit, speedUnit) ->
        getCurrentWeather(q)
    }


    private fun getCurrentWeather(q: String) = flow {
        val response = try {
            api.getCurrentWeather(q)
        } catch (e: IOException) {
            error.value = 1
            return@flow
        } catch (e: HttpException) {
            error.value = 1
            return@flow
        }
        if (response.isSuccessful && response.body() != null) {
            error.value = 0
            emit(response.body()!!)
        } else {
            error.value = 1
        }
    }


    fun changeDegreeUnit() {
        degreeUnit.value = when (degreeUnit.value) {
            "KELVIN" -> "CELSIUS"
            "CELSIUS" -> "FAHRENHEIT"
            "FAHRENHEIT" -> "KELVIN"
            else -> "KELVIN"
        }
    }


    fun changeSpeedUnit() {
        speedUnit.value = when(speedUnit.value) {
            "kmph" -> "mph"
            "mph" -> "kmph"
            else -> "kmph"
        }
    }
}