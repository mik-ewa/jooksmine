package com.example.fitness_tracking_app.features.body_measurement_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import com.example.fitness_tracking_app.common.SharedPreferencesManager.Companion.BMI
import com.example.fitness_tracking_app.data.PersonalData
import com.example.fitness_tracking_app.data.Weights
import com.example.fitness_tracking_app.repo.PersonalInformationRepository
import com.example.fitness_tracking_app.utils.CalculationsUtils
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BodyMeasurementViewModel @Inject constructor(
    private val personalInformationRepository: PersonalInformationRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    private val _personalData = MutableLiveData<Resource<PersonalData?>>()
    val personalData: LiveData<Resource<PersonalData?>> get() = _personalData

    private val _weightList = MutableLiveData<List<Weights>?>()
    val weightList: LiveData<List<Weights>?> get() = _weightList

    private val _bmi = MutableLiveData<String>()
    val bmi: LiveData<String> get() = _bmi

    private var height: Int? = null

    init {
        getPersonalData()
    }

    private fun getPersonalData() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = personalInformationRepository.getUserPersonalDataFromDatabase()
            if (result is Resource.Success) {
                height = result.data?.height
                _weightList.postValue(result.data?.weightList)
            }
            _personalData.postValue(result)
        }
    }

    fun deleteWeight(weight: Weights) {
        viewModelScope.launch(Dispatchers.IO) {
            personalInformationRepository.deleteWeight(weight)
        }
    }

    fun addWeight(weight: Weights) {
        viewModelScope.launch(Dispatchers.IO) {
            personalInformationRepository.addWeight(weight = weight)
        }
    }

    fun calculateBMI(weight: Double?) {
        viewModelScope.launch(Dispatchers.Default) {
            if (height != null && weight != null) {
                val bmi = CalculationsUtils.calculateBMI(height = height!!, weight = weight)
                sharedPreferencesManager.saveString(BMI, bmi)
                _bmi.postValue(bmi)
            } else {
                sharedPreferencesManager.clearSharedPrefsSpecificData(BMI)
            }
        }
    }
}