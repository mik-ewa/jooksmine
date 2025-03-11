package com.example.fitness_tracking_app.features.activity_splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentUser = MutableLiveData<Boolean>()
    val currentUser: LiveData<Boolean> = _currentUser

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _currentUser.postValue(authRepository.isUserLoggedIn())
        }
    }
}
