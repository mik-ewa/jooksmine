package com.example.fitness_tracking_app.di

import android.content.Context
import com.example.fitness_tracking_app.common.NetworkTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkTracker(@ApplicationContext context: Context) : NetworkTracker { return NetworkTracker(context) }
}