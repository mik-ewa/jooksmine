package com.example.fitness_tracking_app.features.home

import com.example.fitness_tracking_app.models.WeatherResponse

sealed class HomeRecyclerItem {

    data class WeatherItem(
        val feelsLike: Double,
        val humidity: Int,
        val temp: Double,
        val description: String,
        val main: String,
        val date: String,
        val wind: Double,
        val city: String? = null
    ) : HomeRecyclerItem() {
        companion object {
            fun createFromWeatherResponse(
                weatherResponse: WeatherResponse,
                date: String,
            ): WeatherItem {
                val weather = weatherResponse.data[0]
                return WeatherItem(
                    feelsLike = weather.feels_like,
                    humidity = weather.humidity,
                    temp = weather.temp,
                    description = weather.weather[0].description,
                    main = weather.weather[0].main,
                    wind = weather.wind_speed,
                    date = date
                )
            }
        }
    }

    data class MotivationItem(val motivation: String?) : HomeRecyclerItem()

    data class ActivitiesItem(val date: String, val duration: String)  : HomeRecyclerItem()

}