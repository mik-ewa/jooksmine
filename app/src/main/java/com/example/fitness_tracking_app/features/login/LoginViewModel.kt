package com.example.fitness_tracking_app.features.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.repo.AuthRepository
import com.example.fitness_tracking_app.utils.Resource
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginStatus = MutableLiveData<Resource<AuthResult>>()
    val loginStatus: LiveData<Resource<AuthResult>> = _loginStatus

    fun login(email: String, password: String) {
        _loginStatus.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            val result = authRepository.login(email, password)
            _loginStatus.postValue(result)
        }
    }
}