package com.hera.weatherapp.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hera.weatherapp.data.WeatherApi
import com.hera.weatherapp.data.models.WeatherResponse
import com.hera.weatherapp.util.Measure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val api: WeatherApi,
    @Named("default")
    private val defaultWeatherResponse: WeatherResponse,
    @Named("error")
    private val errorWeatherResponse: WeatherResponse
) : ViewModel() {

    var q: String = ""
    var measure = Measure.KELVIN
    val weather = MutableLiveData(defaultWeatherResponse)


    fun getCurrentWeather(q: String) = viewModelScope.launch {
        try {
            weather.value = api.getCurrentWeather(q)
        } catch (e: HttpException) {
            weather.value = errorWeatherResponse
            Log.e("TAG", "$e")
        }
    }
}