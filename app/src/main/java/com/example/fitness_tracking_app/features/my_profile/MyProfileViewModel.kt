package com.example.fitness_tracking_app.features.my_profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.data.FirebaseRunData
import com.example.fitness_tracking_app.data.RunActivitiesList
import com.example.fitness_tracking_app.data.UserData
import com.example.fitness_tracking_app.repo.RunningRepository
import com.example.fitness_tracking_app.repo.UserRepository
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val runningRepository: RunningRepository
) : ViewModel() {

    private val _userResult = MutableLiveData<Resource<UserData?>>()
    val userResult: LiveData<Resource<UserData?>> get() = _userResult

    private val _runList = MutableLiveData<Resource<RunActivitiesList>>()
    val runList: LiveData<Resource<RunActivitiesList>> get() = _runList

    private val _photoResult = MutableLiveData<Resource<String>>()
    val photoResult: LiveData<Resource<String>> get() = _photoResult

    private val _backgroundPhotoResult = MutableLiveData<Resource<String>>()
    val backgroundPhotoResult: LiveData<Resource<String>> get() = _backgroundPhotoResult

    init { getUser() }

    private fun getUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val userData = userRepository.getUserFromDatabase()
            _userResult.postValue(userData)
            if (userData is Resource.Success) {
                getRunsWithPhoto(userData.data?.runsList, userData.data?.profilePhoto)
            }
        }
    }

    private suspend fun getRunsWithPhoto(runs: List<String>?, userPhoto: String?) {
        _runList.postValue(Resource.Loading())
        val result = runningRepository.getUserRuns(runs)
        val resultWithPhoto = RunActivitiesList.createFromFirebaseRunDataList(result, userPhoto)
        _runList.postValue(Resource.Success(resultWithPhoto))
    }

    fun saveProfilePhoto(uri: Uri) {
        _photoResult.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            val result = userRepository.saveProfilePhoto(uri)
            _photoResult.postValue(result)
        }
    }

    fun saveBackgroundPhoto(uri: Uri) {
        _backgroundPhotoResult.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            val result = userRepository.saveBackgroundPhoto(uri)
            _backgroundPhotoResult.postValue(result)
        }
    }
}