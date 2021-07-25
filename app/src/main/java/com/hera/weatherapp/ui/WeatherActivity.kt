package com.hera.weatherapp.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.hera.weatherapp.data.models.CurrentWeatherResponse
import com.hera.weatherapp.databinding.ActivityWeatherBinding
import com.hera.weatherapp.util.*
import com.hera.weatherapp.util.Collections.bgMap
import com.hera.weatherapp.util.Collections.iconMap
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

@AndroidEntryPoint
class WeatherActivity : AppCompatActivity(), LocationListener {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var degreeUnitKey: Preferences.Key<String>
    private lateinit var degreeUnitFlow: Flow<String>
    private lateinit var speedUnitKey: Preferences.Key<String>
    private lateinit var speedUnitFlow: Flow<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        degreeUnitKey = stringPreferencesKey("degreeUnit")
        degreeUnitFlow = applicationContext.dataStore.data.map { preferences ->
            preferences[degreeUnitKey] ?: "KELVIN"
        }
        speedUnitKey = stringPreferencesKey("speedUnit")
        speedUnitFlow = applicationContext.dataStore.data.map { preferences ->
            preferences[speedUnitKey] ?: "kmph"
        }
        lifecycleScope.launch {
            degreeUnitFlow.collect {
                viewModel.degreeUnit.value = it
            }
        }
        lifecycleScope.launch {
            speedUnitFlow.collect {
                viewModel.speedUnit.value = it
            }
        }
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLocation(this)
        binding.apply {
            svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.apply {
                        q.value = query ?: ""
                        startLoading(scrollView, pbLoading, tvSearchError, tvProviderDisabled)
                    }
                    hideKeyboard()
                    return true
                }

                override fun onQueryTextChange(newText: String?) = false
            })
            tvTempMain.setOnClickListener {
                viewModel.apply {
                    changeDegreeUnit()
                    startLoading(scrollView, pbLoading, tvSearchError, tvProviderDisabled)
                }
                lifecycleScope.launch {
                    applicationContext.dataStore.edit { settings ->
                        settings[degreeUnitKey] = viewModel.degreeUnit.value
                    }
                }
            }
            tvWindSpeed.setOnClickListener {
                viewModel.apply {
                    changeSpeedUnit()
                    startLoading(scrollView, pbLoading, tvSearchError, tvProviderDisabled)
                }
                lifecycleScope.launch {
                    applicationContext.dataStore.edit { settings ->
                        settings[speedUnitKey] = viewModel.speedUnit.value
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.weather.collect { weatherResponse ->
                updateUI(weatherResponse)
            }
        }
        lifecycleScope.launch {
            viewModel.error.collect {
                binding.apply {
                    if (it == 1) {
                        showSearchError(scrollView, pbLoading, tvSearchError, tvProviderDisabled)
                        viewModel.error.value = 0
                    }
                }
            }
        }
    }


    private fun updateUI(weatherResponse: CurrentWeatherResponse) {
        val coord = weatherResponse.coord
        val weather = weatherResponse.weather[0]
        val dayNight = weather.icon[2]
        val main = weatherResponse.main
        val wind = weatherResponse.wind
        binding.apply {
            bgMap[dayNight]?.let { constraintLayoutWeather.setBackgroundResource(it) }
            tvCityName.text = weatherResponse.name
            tvCityCoord.text = "lat: ${coord.lat}  lon: ${coord.lon}"
            tvDescription.text = weather.description
            Picasso.get()
                    .load(iconMap[weather.icon]!!)
                    .placeholder(iconMap[weather.icon]!!)
                    .into(ivIcon)
            when (viewModel.degreeUnit.value) {
                "KELVIN" -> {
                    tvTempMain.text = main.tempK
                    tvTempFeelsLike.text = main.tempFeelsLikeK
                    tvTempMin.text = main.tempMinK
                    tvTempMax.text = main.tempMaxK
                }
                "CELSIUS" -> {
                    tvTempMain.text = main.tempC
                    tvTempFeelsLike.text = main.tempFeelsLikeC
                    tvTempMin.text = main.tempMinC
                    tvTempMax.text = main.tempMaxC
                }
                "FAHRENHEIT" -> {
                    tvTempMain.text = main.tempF
                    tvTempFeelsLike.text = main.tempFeelsLikeF
                    tvTempMin.text = main.tempMinF
                    tvTempMax.text = main.tempMaxF
                }
            }
            tvHumidityPercent.text = main.humidityString
            when(viewModel.speedUnit.value) {
                "kmph" -> tvWindSpeed.text = wind.speedKmpH
                "mph" -> tvWindSpeed.text = wind.speedMpH
            }
            stopLoading(scrollView, pbLoading, tvSearchError, tvProviderDisabled)
        }
    }


    override fun onLocationChanged(location: Location) {
        try {
            val geocoder = Geocoder(applicationContext, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            viewModel.apply {
                binding.apply {
                    startLoading(scrollView, pbLoading, tvSearchError, tvProviderDisabled)
                }
                q.value = addresses[0].locality
                Log.d("TAG", q.value)
            }
        } catch (e: IOException) {
            Log.e("TAG", "$e")
        }
    }

    override fun onProviderDisabled(provider: String) {
        AlertDialog.Builder(this)
                .setTitle("Location is Disabled")
                .setMessage("Please enable the location so we can show the weather of your current position.")
                .setCancelable(false)
                .setPositiveButton("Enable") { dialog, _ ->
                    dialog.dismiss()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    binding.apply {
                        onCancelToEnableProvider(scrollView, pbLoading, tvSearchError, tvProviderDisabled)
                    }
                }
                .show()
    }
}