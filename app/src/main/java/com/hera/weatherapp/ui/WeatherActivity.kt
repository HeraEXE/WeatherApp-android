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
import android.view.View
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
import com.hera.weatherapp.util.extensions.*
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
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
    private var isObserving = false
    private var searchJob: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataStorePrefs()
        setupBinding()
        getLocation(this)
        getDegreeUnitFromPrefs()
        getSpeedUnitFromPrefs()
        getPreservedOrLoadNewData()
        binding.svSearch.setup()
        onTempMainClick()
        onWindSpeedClick()
    }


    private fun setupDataStorePrefs() {
        degreeUnitKey = stringPreferencesKey("degreeUnit")
        degreeUnitFlow = applicationContext.dataStore.data.map { preferences ->
            preferences[degreeUnitKey] ?: "KELVIN"
        }
        speedUnitKey = stringPreferencesKey("speedUnit")
        speedUnitFlow = applicationContext.dataStore.data.map { preferences ->
            preferences[speedUnitKey] ?: "kmph"
        }
    }


    private fun setupBinding() {
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    private fun getDegreeUnitFromPrefs() = lifecycleScope.launch {
        degreeUnitFlow.collect {
            viewModel.degreeUnit = it
        }
    }


    private fun getSpeedUnitFromPrefs() = lifecycleScope.launch {
        speedUnitFlow.collect {
            viewModel.speedUnit = it
        }
    }


    private fun setDegreeUnitToPrefs() = lifecycleScope.launch {
        applicationContext.dataStore.edit { settings ->
            settings[degreeUnitKey] = viewModel.degreeUnit
        }
    }


    private fun setSpeedUnitToPrefs() = lifecycleScope.launch {
        applicationContext.dataStore.edit { settings ->
            settings[speedUnitKey] = viewModel.speedUnit
        }
    }


    private fun SearchView.setup() {
        this.queryHint = "Search..."
        this.setOnQueryListener { query ->
            searchJob?.cancel()
            searchJob = MainScope().launch {
                delay(500)
                if (query.isNotEmpty())
                    viewModel.query.value = query
                if(!isObserving)
                    observeWeather()
            }
        }
    }


    private fun onTempMainClick() {
        binding.tvTempMain.setOnClickListener {
            viewModel.changeDegreeUnit()
            setDegreeUnitToPrefs()
            onDegreeUnitChange()
        }
    }


    private fun onWindSpeedClick() {
        binding.tvWindSpeed.setOnClickListener {
            viewModel.changeSpeedUnit()
            setSpeedUnitToPrefs()
            onSpeedUnitChange()
        }
    }


    private fun getPreservedOrLoadNewData() {
        if (viewModel.currentWeather != null)
            updateUI()
        else
            observeWeather()
    }


    private fun observeWeather() = lifecycleScope.launch {
        if (!isObserving)
            isObserving = true
        viewModel.weatherFlow.collect { response ->
            when(response) {
                is Resource.Loading -> onLoading()
                is Resource.Error -> onError(response.message!!)
                is Resource.Success -> onSuccess(response.data!!)
            }
        }
    }


    private fun onLoading() {
        binding.pbLoading.visibility = View.VISIBLE
    }


    private fun onError(message: String) {
        binding.apply {
            pbLoading.visibility = View.GONE
            llProviderDisabled.visibility = View.GONE
            scrollView.visibility = View.GONE
            if (message == "404") {
                llNoInternet.visibility = View.GONE
                llNotFound.visibility = View.VISIBLE
            }
            else {
                llNotFound.visibility = View.GONE
                llNoInternet.visibility = View.VISIBLE
            }
        }
    }


    private fun onSuccess(currentWeather: CurrentWeatherResponse) {
        binding.apply {
            pbLoading.visibility = View.GONE
            llProviderDisabled.visibility = View.GONE
            llNotFound.visibility = View.GONE
            llNoInternet.visibility = View.GONE
            scrollView.visibility = View.VISIBLE
        }
        viewModel.currentWeather = currentWeather
        updateUI()
    }


    private fun updateUI() {
        viewModel.apply {
            val coord = currentWeather?.coord
            val weather = currentWeather?.weather?.get(0)
            val dayNight = weather?.icon?.get(2)
            val main = currentWeather?.main
            binding.apply {
                bgMap[dayNight]?.let { constraintLayoutWeather.setBackgroundResource(it) }
                tvCityName.text = currentWeather?.name
                tvCityCoord.text = "lat: ${coord?.lat}  lon: ${coord?.lon}"
                tvDescription.text = weather?.description
                ivIcon.loadDrawable(iconMap[weather?.icon]!!)
                onDegreeUnitChange()
                tvHumidityPercent.text = main?.humidityString
                onSpeedUnitChange()
            }
        }
    }


    private fun onDegreeUnitChange() {
        val main = viewModel.currentWeather?.main
        binding.apply {
            when (viewModel.degreeUnit) {
                "KELVIN" -> {
                    tvTempMain.text = main?.tempK
                    tvTempFeelsLike.text = main?.tempFeelsLikeK
                    tvTempMin.text = main?.tempMinK
                    tvTempMax.text = main?.tempMaxK
                }
                "CELSIUS" -> {
                    tvTempMain.text = main?.tempC
                    tvTempFeelsLike.text = main?.tempFeelsLikeC
                    tvTempMin.text = main?.tempMinC
                    tvTempMax.text = main?.tempMaxC
                }
                "FAHRENHEIT" -> {
                    tvTempMain.text = main?.tempF
                    tvTempFeelsLike.text = main?.tempFeelsLikeF
                    tvTempMin.text = main?.tempMinF
                    tvTempMax.text = main?.tempMaxF
                }
            }
        }
    }


    private fun onSpeedUnitChange() {
        val wind = viewModel.currentWeather?.wind
        binding.apply {
            when(viewModel.speedUnit) {
                "kmph" -> tvWindSpeed.text = wind?.speedKmpH
                "mph" -> tvWindSpeed.text = wind?.speedMpH
            }
        }
    }


    override fun onLocationChanged(location: Location) {
        try {
            val geocoder = Geocoder(applicationContext, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            viewModel.apply {
                query.value = addresses[0].locality
                Log.d("TAG", query.value)
                if (!isObserving)
                    observeWeather()
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
                    updateUiOnProviderDisabled()
                }
                .show()
    }


    override fun onProviderEnabled(provider: String) {
        observeWeather()
    }


    private fun updateUiOnProviderDisabled() {
        binding.apply {
            pbLoading.visibility = View.GONE
            scrollView.visibility = View.GONE
            llNotFound.visibility = View.GONE
            llNoInternet.visibility = View.GONE
            llProviderDisabled.visibility = View.VISIBLE
        }
    }
}