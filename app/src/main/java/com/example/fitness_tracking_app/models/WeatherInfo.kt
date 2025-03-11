package com.example.fitness_tracking_app.models

data class WeatherInfo(
    val feelsLike: Double,
    val humidity: Int,
    val temp: Double,
    val description: String,
    val main: String
)