package com.hera.weatherapp.util.extensions

import android.widget.ImageView
import com.hera.weatherapp.util.Collections
import com.squareup.picasso.Picasso

fun ImageView.loadDrawable(drawable: Int) {
    Picasso.get()
            .load(drawable)
            .placeholder(drawable)
            .into(this)
}