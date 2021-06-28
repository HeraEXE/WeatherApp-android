package com.hera.weatherapp.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.hera.weatherapp.R
import com.hera.weatherapp.data.models.WeatherResponse
import com.hera.weatherapp.databinding.ActivityWeatherBinding
import com.hera.weatherapp.util.Collections.bgMap
import com.hera.weatherapp.util.Collections.iconMap
import com.hera.weatherapp.util.Measure
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import java.util.*

@AndroidEntryPoint
class WeatherActivity : AppCompatActivity(), LocationListener {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var locationManager: LocationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLocationEnabled()
        getLocation()


        binding.apply {
            svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.q = query ?: ""
                    startLoading()
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

        viewModel.error.observe(this) {
            AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("No such city exists")
                    .setMessage("Try againg")
                    .setNeutralButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        }
    }


    private fun changeMeasureUnit() {
        viewModel.measure = when (viewModel.measure) {
            Measure.KELVIN -> Measure.CELSIUS
            Measure.CELSIUS -> Measure.FAHRENHEIT
            Measure.FAHRENHEIT -> Measure.KELVIN
        }
        startLoading()
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
        stopLoading()
    }


    private fun checkLocationEnabled() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var isGpsEnabled = false
        var isNetworkEnabled = false

        try {
            isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            Log.e("TAG", "$e")
        }

        try {
            isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            Log.e("TAG", "$e")
        }

        if (!isGpsEnabled && !isNetworkEnabled) {
            AlertDialog.Builder(this)
                    .setTitle("Enable Location")
                    .setCancelable(false)
                    .setPositiveButton("Enable") { dialog, _ ->
                        dialog.dismiss()
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        }
    }


    private fun getLocation() {
        try {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "not ok")
                ActivityCompat.requestPermissions(
                        this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                        1
                )
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5f, this)
        } catch (e: SecurityException) {
            Log.e("TAG", "$e")
            CoroutineScope(Main).launch {
                delay(1000L)
                getLocation()
            }
        }
    }


    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = activity.currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(
                    currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    override fun onLocationChanged(location: Location) {
        try {
            val geocoder = Geocoder(applicationContext, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            startLoading()
            viewModel.q = addresses[0].locality
            viewModel.getCurrentWeather(viewModel.q)
            Log.d("TAG", viewModel.q)
            Log.d("TAG", viewModel.weather.value.toString())
        } catch (e: IOException) {
            Log.e("TAG", "$e")
        }
    }


    private fun startLoading() {
        binding.scrollView.visibility = View.INVISIBLE
        binding.pbLoading.visibility = View.VISIBLE
    }


    private fun stopLoading() {
        binding.scrollView.visibility = View.VISIBLE
        binding.pbLoading.visibility = View.INVISIBLE
    }
}