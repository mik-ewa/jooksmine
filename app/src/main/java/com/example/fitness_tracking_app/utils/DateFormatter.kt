package com.example.fitness_tracking_app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {

    fun secondsToTime(seconds: Int) : String {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.SECOND, seconds)
        if (seconds<3600) {
            val sdf = SimpleDateFormat("mm:ss", Locale.getDefault())
            return sdf.format(calendar.time)
        }
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    fun timeMillisToDate(date: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val resultDate = Date(date)
        return sdf.format(resultDate)
    }

    fun timeMillisToDateTime(dateMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(dateMillis))
    }

    fun timeMillisToMessageTime(date: Long): String {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis

        if (date >= startOfDay) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(date))
        }

        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val oneWeekAgo = calendar.timeInMillis

        if (date >= oneWeekAgo) {
            val sdf = SimpleDateFormat("EEE", Locale.getDefault())
            return sdf.format(Date(date))
        }

        calendar.set(Calendar.MONTH, Calendar.JANUARY)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)

        val firstDayOfYear = calendar.timeInMillis

        if (date >= firstDayOfYear) {
            val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
            return sdf.format(Date(date))
        }

        val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return sdf.format(Date(date))
    }
}