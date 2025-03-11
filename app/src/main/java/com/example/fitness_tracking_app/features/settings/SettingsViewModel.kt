package com.example.fitness_tracking_app.features.settings

import androidx.lifecycle.ViewModel
import com.example.fitness_tracking_app.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {


}