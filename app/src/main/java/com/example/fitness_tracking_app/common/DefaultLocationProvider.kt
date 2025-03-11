package com.example.fitness_tracking_app.common

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.mapbox.common.location.LocationProviderRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : LocationProvider {

    override fun getLocationUpdates(
        request: LocationProviderRequest,
        requiredPermission: String,
        onLocationReceived: (Location) -> Unit
    ): LocationCallback {
        if (ContextCompat.checkSelfPermission(context, requiredPermission) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("Permission $requiredPermission is not granted")
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    onLocationReceived(location)
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            request.toLocationRequest(),
            locationCallback,
            Looper.getMainLooper()
        )

        return locationCallback
    }

    override fun removeLocationUpdates(callback: LocationCallback) {
        fusedLocationProviderClient.removeLocationUpdates(callback)
    }

    private fun LocationProviderRequest.toLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval?.interval!!)
            .setMinUpdateIntervalMillis(interval?.minimumInterval!!)
            .setMaxUpdateDelayMillis(interval?.maximumInterval!!)
            .build()
    }
}
