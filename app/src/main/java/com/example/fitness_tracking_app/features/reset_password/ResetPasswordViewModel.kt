package com.example.fitness_tracking_app.features.reset_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.repo.AuthRepository
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {

    private val _resetPasswordResult = MutableLiveData<Resource<Unit>>()
    val resetPasswordResult: LiveData<Resource<Unit>> = _resetPasswordResult

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = authRepository.authResetPasswordWithEmail(email)
            _resetPasswordResult.postValue(result)
        }
    }
}
