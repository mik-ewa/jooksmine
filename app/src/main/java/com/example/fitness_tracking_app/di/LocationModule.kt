package com.example.fitness_tracking_app.di

import android.content.Context
import com.example.fitness_tracking_app.common.DefaultLocationProvider
import com.example.fitness_tracking_app.common.LocationHelper
import com.example.fitness_tracking_app.common.LocationProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationProvider(
        fusedLocationProviderClient: FusedLocationProviderClient,
        @ApplicationContext context: Context
    ): LocationProvider {
        return DefaultLocationProvider(context, fusedLocationProviderClient)
    }

    @Provides
    @Singleton
    fun provideLocationHelper() = LocationHelper()
}
