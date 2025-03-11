package com.example.fitness_tracking_app.models

data class WeatherResponse(
    val `data`: List<Data>,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: Int
)