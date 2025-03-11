package com.example.fitness_tracking_app.repo

import com.example.fitness_tracking_app.api.WeatherApi
import com.example.fitness_tracking_app.models.WeatherResponse
import retrofit2.Response

class WeatherRepository(private val weatherApi: WeatherApi) : IWeatherRepository {

    override suspend fun getCurrentWeather(
        long: Double,
        lat: Double,
        time: Long
    ): Response<WeatherResponse> = weatherApi.getCurrentWeather(latitude = lat, longitude = long, time = time)
}