package com.hera.weatherapp.ui

import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.ScrollView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hera.weatherapp.data.WeatherApi
import com.hera.weatherapp.data.models.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val api: WeatherApi
) : ViewModel() {

    var q: String = ""
    var unit = "KELVIN"
    val weather = MutableLiveData<WeatherResponse>()
    val error = MutableLiveData<Int>()


    fun getCurrentWeather(q: String) = viewModelScope.launch {
        try {
            weather.value = api.getCurrentWeather(q)
        } catch (e: HttpException) {
            Log.e("TAG", "$e")
            error.value = error.value?.plus(1)
        }
    }


    fun changeMeasureUnit(scrollView: ScrollView, progressBar: ProgressBar) {
        unit = when (unit) {
            "KELVIN" -> "CELSIUS"
            "CELSIUS" -> "FAHRENHEIT"
            "FAHRENHEIT" -> "KELVIN"
            else -> "KELVIN"
        }
        startLoading(scrollView, progressBar)
        getCurrentWeather(q)
    }


    fun startLoading(scrollView: ScrollView, progressBar: ProgressBar) {
        scrollView.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }


    fun stopLoading(scrollView: ScrollView, progressBar: ProgressBar) {
        scrollView.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
    }
}