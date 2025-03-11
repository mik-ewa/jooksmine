package com.example.fitness_tracking_app.features.motivation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.database.Motivation
import com.example.fitness_tracking_app.features.MotivationOption
import com.example.fitness_tracking_app.repo.PersonalInformationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotivationViewModel @Inject constructor(
    private val personalInformationRepository: PersonalInformationRepository
) : ViewModel() {

    private val _motivation = MutableLiveData<Motivation?>()
    val motivation : LiveData<Motivation?> get() = _motivation

    init { showMotivation() }

    private fun showMotivation(){
        viewModelScope.launch(Dispatchers.IO) {
            personalInformationRepository.getUserFlow().mapNotNull {
                it
            }.collect {
                _motivation.postValue(it.motivation)
            }
        }
    }

    fun saveMotivation(motivationOption: MotivationOption, customText: String? = null) {
        val motivation = when (motivationOption) {
            MotivationOption.CUSTOM -> Motivation(
                key = motivationOption.key,
                customText = customText
            )

            else -> Motivation(key = motivationOption.key, customText = null)
        }
        viewModelScope.launch(Dispatchers.IO) {
            personalInformationRepository.saveMotivation(motivation)
        }
    }
}