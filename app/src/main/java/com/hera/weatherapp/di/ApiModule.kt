package com.hera.weatherapp.di

import com.hera.weatherapp.data.WeatherApi
import com.hera.weatherapp.data.models.Main
import com.hera.weatherapp.data.models.Weather
import com.hera.weatherapp.data.models.WeatherResponse
import com.hera.weatherapp.util.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideRetrofit() = Retrofit
        .Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    @Provides
    fun provideWeatherApi(
        retrofit: Retrofit
    ) = retrofit
        .create(WeatherApi::class.java)
}