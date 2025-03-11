package com.example.fitness_tracking_app.common

import android.content.Context
import android.content.SharedPreferences
import com.example.fitness_tracking_app.data.BaseRunData
import com.example.fitness_tracking_app.features.home.HomeRecyclerItem
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    companion object {
        const val LANGUAGE = "language"
        const val PREFIX = "prefix"
        const val TRACKING = "tracking"
        const val BMI = "bmi"
        const val MODE_TYPE = "mode_type"
        const val RUN_ID = "run_id"
        const val BASE_RUN_DATA = "base_run_data"
        const val WEATHER_DATA = "weather_data"
        const val AUTOMATIC_MESSAGE = "automatic_message"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MY_SHARED_PREFS", Context.MODE_PRIVATE)

    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String) : Boolean {
        return sharedPreferences.getBoolean(key, false) ?: false
    }

    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun clearSharedPrefsSpecificData(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun saveRunBaseData(runData: BaseRunData) {
        val json = Gson().toJson(runData)
        saveString(BASE_RUN_DATA, json)
    }

    fun getRunBaseData(): BaseRunData? {
        val json = getString(BASE_RUN_DATA, "")
        return if (json.isNotEmpty()) Gson().fromJson(json, BaseRunData::class.java) else null
    }

    fun saveWeatherData(weatherItem: HomeRecyclerItem.WeatherItem) {
        val json = Gson().toJson(weatherItem)
        saveString(WEATHER_DATA, json)
    }

    fun getWeatherData() : HomeRecyclerItem.WeatherItem? {
        val json = getString(WEATHER_DATA, "")
        return if (json.isNotEmpty()) Gson().fromJson(json, HomeRecyclerItem.WeatherItem::class.java) else null
    }

    fun clearSharedPrefsAllData() {
        sharedPreferences.edit().clear().apply()
    }
}
