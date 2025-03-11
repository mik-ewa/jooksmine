package com.example.fitness_tracking_app.di

import android.app.Activity
import com.example.fitness_tracking_app.common.DialogManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object DialogModule {

    @Provides
    fun provideDialogManager(activity: Activity): DialogManager {
        return DialogManager(activity)
    }
}
