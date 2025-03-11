package com.example.fitness_tracking_app.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MotivationConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromMotivation(motivation: Motivation?): String {
        return gson.toJson(motivation)
    }

    @TypeConverter
    fun toMotivation(json: String): Motivation? {
        val type = object : TypeToken<Motivation>() {}.type
        return gson.fromJson(json, type)
    }
}
