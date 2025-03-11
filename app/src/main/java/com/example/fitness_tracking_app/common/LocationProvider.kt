package com.example.fitness_tracking_app.common

import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.mapbox.common.location.LocationProviderRequest

interface LocationProvider {
    fun getLocationUpdates(
        request: LocationProviderRequest,
        requiredPermission: String,
        onLocationReceived: (Location) -> Unit
    ): LocationCallback

    fun removeLocationUpdates(callback: LocationCallback)
}
