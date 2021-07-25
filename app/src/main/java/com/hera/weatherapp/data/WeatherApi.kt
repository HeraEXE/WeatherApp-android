package com.hera.weatherapp.data

import com.hera.weatherapp.BuildConfig
import com.hera.weatherapp.data.models.CurrentWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("/data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") q: String,
        @Query("appid") appid: String = BuildConfig.API_KEY
    ): Response<CurrentWeatherResponse>
}