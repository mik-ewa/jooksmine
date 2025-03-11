package com.example.fitness_tracking_app.features.activity_main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import com.example.fitness_tracking_app.repo.AuthRepository
import com.example.fitness_tracking_app.repo.UserRepository
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    private val _ifUserExists = MutableLiveData<Resource<Boolean?>>()
    val ifUserExists: LiveData<Resource<Boolean?>> get() = _ifUserExists

    init {
        getUserFromDatabase()
    }

    fun signOut() {
        sharedPreferencesManager.clearSharedPrefsAllData()
        authRepository.signOut()
    }

    private fun getUserFromDatabase() {
        viewModelScope.launch(Dispatchers.Main) {
            _ifUserExists.postValue(Resource.Loading())
            _ifUserExists.postValue(userRepository.ifUserExists())
        }
    }

    fun setMyActiveStatus(isActive: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.setStatus(isActive)
        }
    }
}