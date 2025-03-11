package com.example.fitness_tracking_app.api

import com.example.fitness_tracking_app.BuildConfig
import com.example.fitness_tracking_app.models.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("onecall/timemachine")
    suspend fun getCurrentWeather(
    @Query("lat") latitude: Double,
    @Query("lon") longitude: Double,
    @Query("dt") time: Long,
    @Query("units") units : String = "metric",
    @Query("lang") language: String = "en",
    @Query("appid") apiKey: String = BuildConfig.WEATHER_API_KEY
    ): Response<WeatherResponse>
}
