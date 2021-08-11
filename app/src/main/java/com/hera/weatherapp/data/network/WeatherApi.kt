package com.hera.weatherapp.data.network

import com.hera.weatherapp.data.models.CurrentWeatherResponse
import com.hera.weatherapp.util.Constants.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("/data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") q: String,
        @Query("appid") appid: String = API_KEY
    ): Response<CurrentWeatherResponse>
}