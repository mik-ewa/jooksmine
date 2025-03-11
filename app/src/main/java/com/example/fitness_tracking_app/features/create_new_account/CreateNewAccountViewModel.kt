package com.example.fitness_tracking_app.features.create_new_account

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
class CreateNewAccountViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerStatus = MutableLiveData<Resource<AuthResult>>()
    val registerStatus: LiveData<Resource<AuthResult>> = _registerStatus

    fun createUserWithEmailAndPassword(email: String, password: String) {
        _registerStatus.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            val result = authRepository.registerUser(email = email, password = password)
            _registerStatus.postValue(result)
        }
    }
}
