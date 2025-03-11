package com.example.fitness_tracking_app.di

import android.app.Application
import android.content.Context
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ToolsModule {

    @Provides
    @Singleton
    fun provideContext(application: Application) = application.applicationContext

    @Provides
    @Singleton
    fun provideSharedPreferencesManager(@ApplicationContext context: Context): SharedPreferencesManager {
        return SharedPreferencesManager(context)
    }
}