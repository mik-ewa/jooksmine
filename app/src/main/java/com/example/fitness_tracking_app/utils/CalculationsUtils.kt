package com.example.fitness_tracking_app.utils

import android.annotation.SuppressLint
import kotlin.math.roundToInt

object CalculationsUtils {

    fun calculateBMI(height: Int, weight: Double): String {
        val heightCM = height / 100.toDouble()
        val squareHeight = (heightCM * heightCM)
        val bmi = weight / squareHeight
        return bmi.toString()
    }

    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0]
    }

    @SuppressLint("DefaultLocale")
    fun formatPace(paceInMinutes: Float): String {
        val paceMinutes = paceInMinutes.toInt()
        val paceSeconds = ((paceInMinutes - paceMinutes) * 60).roundToInt()
        return String.format("%d:%02d/km", paceMinutes, paceSeconds % 60)
    }

    fun calculatePaceInMinutes(seconds: Int, meters: Double): Float {
        val minutes = seconds / 60.0
        val kilometers = meters / 1000.0
        return if (kilometers > 0) (minutes / kilometers).toFloat() else 0f
    }

    fun calculateMetersToKilometers(meters: Double): Double  = meters / 1000
}
