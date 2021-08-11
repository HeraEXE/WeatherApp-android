package com.hera.weatherapp.util.extensions

import androidx.appcompat.widget.SearchView

fun SearchView.setOnQueryListener(listener: (String) -> Unit) {
    this.setOnQueryTextListener(
        object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    listener(newText)
                }
                return true
            }
        }
    )
}