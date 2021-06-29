package com.hera.weatherapp.util

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception


fun AppCompatActivity.hideKeyboard() {
    val inputMethodManager =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocusedView = this.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}


fun AppCompatActivity.checkLocationEnabled() {
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


fun AppCompatActivity.getLocation(locationListener: LocationListener) {
    try {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5f, locationListener)
    } catch (e: SecurityException) {
        Log.e("TAG", "$e")
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000L)
            getLocation(locationListener)
        }
    }
}