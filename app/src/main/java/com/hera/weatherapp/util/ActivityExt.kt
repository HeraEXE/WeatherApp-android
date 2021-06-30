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
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
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


fun AppCompatActivity.startLoading(
        scrollView: ScrollView,
        progressBar: ProgressBar,
        errorTextView: TextView,
        providerDisabledTextView: TextView,
) {
    scrollView.visibility = View.INVISIBLE
    progressBar.visibility = View.VISIBLE
    errorTextView.visibility = View.INVISIBLE
    providerDisabledTextView.visibility = View.INVISIBLE
}


fun AppCompatActivity.stopLoading(
        scrollView: ScrollView,
        progressBar: ProgressBar,
        errorTextView: TextView,
        providerDisabledTextView: TextView,
) {
    scrollView.visibility = View.VISIBLE
    progressBar.visibility = View.INVISIBLE
    errorTextView.visibility = View.INVISIBLE
    providerDisabledTextView.visibility = View.INVISIBLE
}


fun AppCompatActivity.showSearchError(
        scrollView: ScrollView,
        progressBar: ProgressBar,
        errorTextView: TextView,
        providerDisabledTextView: TextView,
) {
    scrollView.visibility = View.INVISIBLE
    progressBar.visibility = View.INVISIBLE
    errorTextView.visibility = View.VISIBLE
    providerDisabledTextView.visibility = View.INVISIBLE

}


fun AppCompatActivity.onCancelToEnableProvider(
        scrollView: ScrollView,
        progressBar: ProgressBar,
        errorTextView: TextView,
        providerDisabledTextView: TextView,
) {
    scrollView.visibility = View.INVISIBLE
    progressBar.visibility = View.INVISIBLE
    errorTextView.visibility = View.INVISIBLE
    providerDisabledTextView.visibility = View.VISIBLE
}