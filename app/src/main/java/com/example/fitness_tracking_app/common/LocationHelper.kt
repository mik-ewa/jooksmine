package com.example.fitness_tracking_app.common

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.Locale

class LocationHelper {
    fun getCityName(context: Context, latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address> =
                geocoder.getFromLocation(latitude, longitude, 1)?.toList() ?: emptyList()
            if (addresses.isNotEmpty()) {
                addresses[0].locality
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
