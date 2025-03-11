package com.example.fitness_tracking_app.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.database.entity.User
import com.example.fitness_tracking_app.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserFlowFromDao().collect {
                _userData.postValue(it)
            }
        }
    }
}