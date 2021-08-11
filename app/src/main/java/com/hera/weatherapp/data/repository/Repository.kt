package com.hera.weatherapp.data.repository

import com.hera.weatherapp.data.network.WeatherApi
import com.hera.weatherapp.util.Resource
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val weatherApi: WeatherApi
) {

    fun getCurrentWeather(q: String) = flow {
        emit(Resource.Loading())
        val response = try {
            weatherApi.getCurrentWeather(q)
        } catch (e: IOException) {
            emit(Resource.Error(e.message))
            return@flow
        } catch (e: HttpException) {
            emit(Resource.Error(e.message))
            return@flow
        }
        if (response.isSuccessful && response.body() != null) {
            emit(Resource.Success(response.body()!!))
        } else {
            emit(Resource.Error("404"))
            return@flow
        }
    }
}