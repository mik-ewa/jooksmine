package com.example.fitness_tracking_app.features.delete_account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.repo.AuthRepository
import com.example.fitness_tracking_app.repo.UserRepository
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private var _removeResult = MutableLiveData<Resource<Unit>>()
    val removeResult: LiveData<Resource<Unit>> get() = _removeResult

    fun checkCredentials(password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _removeResult.postValue(Resource.Loading())
            val credentialsResult = authRepository.checkCredentials(password)
            if (credentialsResult is Resource.Success) {
                removeAccount()
            } else {
                _removeResult.postValue(Resource.Error(credentialsResult.message))
            }
        }
    }

    private suspend fun removeAccount() {
        val databaseResult = userRepository.deactivateAccount()
        if (databaseResult is Resource.Success) {
            val authResult = authRepository.removeAccount()
            _removeResult.postValue(authResult)
        } else {
            _removeResult.postValue(Resource.Error(databaseResult.message))
        }
    }
}