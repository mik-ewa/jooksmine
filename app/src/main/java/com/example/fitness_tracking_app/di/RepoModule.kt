package com.example.fitness_tracking_app.di

import com.example.fitness_tracking_app.api.WeatherApi
import com.example.fitness_tracking_app.database.AppDatabase
import com.example.fitness_tracking_app.repo.AuthRepository
import com.example.fitness_tracking_app.repo.ChatRepository
import com.example.fitness_tracking_app.repo.FriendsRepository
import com.example.fitness_tracking_app.repo.RunningRepository
import com.example.fitness_tracking_app.repo.PersonalInformationRepository
import com.example.fitness_tracking_app.repo.UserRepository
import com.example.fitness_tracking_app.repo.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(appDatabase: AppDatabase) = UserRepository(userDao = appDatabase.userDao())

    @Provides
    @Singleton
    fun provideAuthRepository() = AuthRepository()

    @Provides
    @Singleton
    fun provideFriendsRepository() = FriendsRepository()

    @Provides
    @Singleton
    fun provideSettingsRepository(appDatabase: AppDatabase) = PersonalInformationRepository(userDao = appDatabase.userDao())

    @Provides
    @Singleton
    fun provideChatRepository() = ChatRepository()

    @Provides
    @Singleton
    fun provideRunningRepository() = RunningRepository()

    @Provides
    @Singleton
    fun provideWeatherRepo(api: WeatherApi) = WeatherRepository(api)
}