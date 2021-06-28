package com.hera.weatherapp.ui

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import com.hera.weatherapp.R
import com.hera.weatherapp.data.models.WeatherResponse
import com.hera.weatherapp.databinding.ActivityWeatherBinding
import com.hera.weatherapp.util.Collections.bgMap
import com.hera.weatherapp.util.Collections.iconMap
import com.hera.weatherapp.util.Measure
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeatherActivity : AppCompatActivity() {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var binding: ActivityWeatherBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.q = query ?: ""
                    viewModel.getCurrentWeather(viewModel.q)
                    hideKeyboard(this@WeatherActivity)
                    return true
                }
                override fun onQueryTextChange(newText: String?) = false
            })

            tvTempMain.setOnClickListener {
                changeMeasureUnit()
            }
        }

            viewModel.weather.observe(this) { weatherResponse ->
                updateUI(weatherResponse)
            }
    }


    private fun changeMeasureUnit() {
        viewModel.measure = when (viewModel.measure) {
            Measure.KELVIN -> Measure.CELSIUS
            Measure.CELSIUS -> Measure.FAHRENHEIT
            Measure.FAHRENHEIT -> Measure.KELVIN
        }
        viewModel.getCurrentWeather(viewModel.q)
    }


    private fun updateUI(weatherResponse: WeatherResponse) {
        val weather = weatherResponse.weather[0]
        val dayNight = weather.icon[2]
        val main = weatherResponse.main
        binding.apply {
            bgMap[dayNight]?.let { constraintLayoutWeather.setBackgroundResource(it) }
            tvCity.text = weatherResponse.name
            tvDescription.text = weather.main
            tvHumidityPercent.text = main.humidityString
            Picasso.get()
                .load(iconMap[weather.icon]!!)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(ivIcon)
            when (viewModel.measure) {
                Measure.KELVIN -> {
                    tvTempMain.text = main.tempK
                    tvTempMin.text = main.tempMinK
                    tvTempMax.text = main.tempMaxK
                }
                Measure.CELSIUS -> {
                    tvTempMain.text = main.tempC
                    tvTempMin.text = main.tempMinC
                    tvTempMax.text = main.tempMaxC
                }
                Measure.FAHRENHEIT -> {
                    tvTempMain.text = main.tempF
                    tvTempMin.text = main.tempMinF
                    tvTempMax.text = main.tempMaxF
                }
            }
        }
    }


    fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = activity.currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}