package com.example.fitness_tracking_app.features.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.BuildConfig
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import com.example.fitness_tracking_app.common.SharedPreferencesManager.Companion.AUTOMATIC_MESSAGE
import com.example.fitness_tracking_app.common.SharedPreferencesManager.Companion.BMI
import com.example.fitness_tracking_app.data.MsgData
import com.example.fitness_tracking_app.database.Motivation
import com.example.fitness_tracking_app.features.MotivationOption
import com.example.fitness_tracking_app.repo.ChatRepository
import com.example.fitness_tracking_app.repo.PersonalInformationRepository
import com.example.fitness_tracking_app.repo.UserRepository
import com.example.fitness_tracking_app.utils.CalculationsUtils
import com.example.fitness_tracking_app.common.FirebasePaths
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val personalInformationRepository: PersonalInformationRepository,
    private val userRepository: UserRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _motivation = MutableLiveData<Motivation>()
    val motivation: LiveData<Motivation> get() = _motivation

    init {
        checkUserDao()
        if (!sharedPreferencesManager.getBoolean(AUTOMATIC_MESSAGE) && BuildConfig.AUTO_FRIEND_MODE && !userRepository.isMainUser) {
            sendAutomaticMessage()
        }
    }

    private fun checkUserDao() {
        viewModelScope.launch {
            val result = personalInformationRepository.getUser()
            if (result == null) {
                saveUserToLocalBase()
            } else {
                if (result.motivation.key != MotivationOption.NONE.key) {
                    _motivation.postValue(result.motivation)
                }
            }
        }
    }

    private fun sendAutomaticMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = chatRepository.getChatFromMyDatabase(BuildConfig.MAIN_FIREBASE_USER)
            if (result is Resource.Success) {
                if (result.data != null) {
                    sharedPreferencesManager.saveBoolean(AUTOMATIC_MESSAGE, true)
                } else {
                    val msgData = MsgData(friendId = BuildConfig.MAIN_FIREBASE_USER, chatId = UUID.randomUUID().toString())
                    chatRepository.sendAutoMessage(msgData)
                }
            }
        }
    }

    private suspend fun saveUserToLocalBase() {
        val result = userRepository.getUserFromDatabase()
        if (result is Resource.Success) {
            userRepository.saveUserToDao(result.data!!)
            val height = result.data.personalData?.height
            val lastWeight = result.data.personalData?.weightList?.last()?.weight
            if (height != null && lastWeight != null) {
                val bmi = CalculationsUtils.calculateBMI(height = height, weight = lastWeight)
                sharedPreferencesManager.saveString(BMI, bmi)
            }
        }
    }
}