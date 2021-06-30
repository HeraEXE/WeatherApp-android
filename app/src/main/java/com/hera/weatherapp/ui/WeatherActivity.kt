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
import com.hera.weatherapp.R
import com.hera.weatherapp.data.models.WeatherResponse
import com.hera.weatherapp.databinding.ActivityWeatherBinding
import com.hera.weatherapp.util.*
import com.hera.weatherapp.util.Collections.bgMap
import com.hera.weatherapp.util.Collections.iconMap
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.*

@AndroidEntryPoint
class WeatherActivity : AppCompatActivity(), LocationListener {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var binding: ActivityWeatherBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        viewModel.unit = sharedPrefs.getString("unit", "KELVIN")!!

        getLocation(this)

        binding.apply {
            svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.apply {
                        q = query ?: ""
                        startLoading(scrollView, pbLoading, tvSearchError)
                        getCurrentWeather(q)
                    }
                    hideKeyboard()
                    return true
                }

                override fun onQueryTextChange(newText: String?) = false
            })
            tvTempMain.setOnClickListener {
                viewModel.apply {
                    changeMeasureUnit()
                    startLoading(scrollView, pbLoading, tvSearchError)
                    getCurrentWeather(q)
                }
                editor.apply {
                    putString("unit", viewModel.unit)
                    apply()
                }
            }
        }

        viewModel.weather.observe(this) { weatherResponse ->
            updateUI(weatherResponse)
        }

        viewModel.error.observe(this) {
            showSearchError(binding.tvSearchError, binding.pbLoading)
        }
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
            when (viewModel.unit) {
                "KELVIN" -> {
                    tvTempMain.text = main.tempK
                    tvTempMin.text = main.tempMinK
                    tvTempMax.text = main.tempMaxK
                }
                "CELSIUS" -> {
                    tvTempMain.text = main.tempC
                    tvTempMin.text = main.tempMinC
                    tvTempMax.text = main.tempMaxC
                }
                "FAHRENHEIT" -> {
                    tvTempMain.text = main.tempF
                    tvTempMin.text = main.tempMinF
                    tvTempMax.text = main.tempMaxF
                }
            }
            stopLoading(scrollView, pbLoading, tvSearchError)
        }
    }


    override fun onLocationChanged(location: Location) {
        try {
            val geocoder = Geocoder(applicationContext, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            viewModel.apply {
                startLoading(binding.scrollView, binding.pbLoading, binding.tvSearchError)
                q = addresses[0].locality
                getCurrentWeather(q)
                Log.d("TAG", q)
                Log.d("TAG", weather.value.toString())
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
                    onCancelToEnableProvider(binding.tvProviderDisabled, binding.pbLoading)
                }
                .show()
    }
}