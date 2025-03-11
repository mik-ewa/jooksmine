package com.example.fitness_tracking_app.features.tracking

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.common.LocationHelper
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import com.example.fitness_tracking_app.common.SharedPreferencesManager.Companion.RUN_ID
import com.example.fitness_tracking_app.common.SharedPreferencesManager.Companion.TRACKING
import com.example.fitness_tracking_app.data.BaseRunData
import com.example.fitness_tracking_app.data.CompletedRunData
import com.example.fitness_tracking_app.repo.RunningRepository
import com.example.fitness_tracking_app.repo.UserRepository
import com.example.fitness_tracking_app.utils.Converters
import com.example.fitness_tracking_app.common.NetworkTracker
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_NONE
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_PAUSE
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_RESUME
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_START
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_STOP
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val runningRepository: RunningRepository,
    private val userRepository: UserRepository,
    private val locationHelper: LocationHelper,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val networkTracker: NetworkTracker,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _runResult = MutableLiveData<Resource<String>>()
    val runResult: LiveData<Resource<String>> get() = _runResult

    private val _runStatus = MutableLiveData<String>()
    val runStatus: LiveData<String> get() = _runStatus

    fun toggleTrackingStatus(buttonType: ButtonType = ButtonType.DEFAULT) {
        val currentStatus = sharedPreferencesManager.getString(TRACKING, ACTION_NONE)
        when (currentStatus) {
            ACTION_NONE -> {
                if (networkTracker.isConnectedToInternet()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        val result = startRun()
                        if (result is Resource.Success) {
                            sharedPreferencesManager.saveString(RUN_ID, result.data ?: "")
                            _runStatus.postValue(ACTION_START)
                        } else {
                           postError()
                        }
                    }
                } else {
                    postError("You are not connected to internet")
                }
            }
            ACTION_START -> _runStatus.postValue(ACTION_PAUSE)
            ACTION_PAUSE -> if (buttonType == ButtonType.DEFAULT) _runStatus.postValue(ACTION_RESUME) else _runStatus.postValue(ACTION_STOP)
            ACTION_STOP -> if (buttonType == ButtonType.DEFAULT) _runStatus.postValue(ACTION_RESUME) else _runStatus.postValue(ACTION_NONE)
            ACTION_RESUME -> _runStatus.postValue(ACTION_PAUSE)
        }
    }

    private suspend fun startRun(): Resource<String> {
        val startDate = System.currentTimeMillis()
        return runningRepository.startRunning(startDate)
    }

    private fun fetchCityName(longitude: Double, latitude: Double): String? {
       return  locationHelper.getCityName(context, latitude = latitude, longitude = longitude)
    }

    fun insertRunData(baseRunData: BaseRunData, bitmap: Bitmap?) {
        handleRunAction { runId ->
            val place = fetchCityName(longitude = baseRunData.longitude, latitude = baseRunData.latitude)
            val completedRunData = CompletedRunData.createFromBaseRunData(
                baseRunData = baseRunData,
                runId = runId,
                place = place,
                image = Converters.bitmapToBase64(bitmap)
            )
            val result = runningRepository.finishRunning(completedRunData)
            if (result is Resource.Success) {
                userRepository.addActivity(runId)
            } else {
                //TODO:safe to local database
                postError()
            }
        }
    }

    fun deleteRunData() {
        handleRunAction { runId -> runningRepository.removeRunningData(runId) }
    }

    private fun handleRunAction(action: suspend (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val runId = sharedPreferencesManager.getString(RUN_ID, "")
            if (runId.isNotBlank()) {
                action(runId)
                sharedPreferencesManager.clearSharedPrefsSpecificData(RUN_ID)
            } else {
                postError()
            }
        }
    }

    private fun postError(message: String = "Something went wrong") {
        _runResult.postValue(Resource.Error(message))
    }
}
