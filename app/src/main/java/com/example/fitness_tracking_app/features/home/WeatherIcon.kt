package com.example.fitness_tracking_app.features.home

import com.example.fitness_tracking_app.R

enum class WeatherIcon(val main: String, val iconResId: Int) {
    THUNDERSTORM("Thunderstorm", R.drawable.icon_weather_thunderstorm),
    DRIZZLE("Drizzle", R.drawable.icon_weather_rain),
    RAIN("Rain", R.drawable.icon_weather_rain),
    SNOW("Snow", R.drawable.icon_weather_snow),
    CLEAR("Clear", R.drawable.icon_weather_clear),
    CLOUDS("Clouds", R.drawable.icon_weather_clouds),
    MIST("Mist", R.drawable.icon_weather_mist),
    SMOKE("Smoke", R.drawable.icon_weather_mist),
    HAZE("Haze", R.drawable.icon_weather_mist),
    DUST("Dust", R.drawable.icon_weather_mist),
    FOG("Fog", R.drawable.icon_weather_mist),
    SAND("Sand", R.drawable.icon_weather_mist),
    ASH("Ash", R.drawable.icon_weather_mist),
    SQUALL("Squall", R.drawable.icon_weather_thunderstorm),
    TORNADO("Tornado", R.drawable.icon_weather_tornado);

    companion object {
        fun fromMain(main: String): Int {
            return values().find { it.main.equals(main, ignoreCase = true) }?.iconResId
                ?: R.drawable.icon_weather_clear
        }
    }
}
