package com.example.fitness_tracking_app.features.personal_information

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import com.example.fitness_tracking_app.data.PersonalData
import com.example.fitness_tracking_app.repo.PersonalInformationRepository
import com.example.fitness_tracking_app.utils.CalculationsUtils
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalInformationViewModel @Inject constructor(
    private val personalInformationRepository: PersonalInformationRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    private val _personalData = MutableLiveData<PersonalData?>()
    val personalData: LiveData<PersonalData?> = _personalData

    private val _result = MutableLiveData<Resource<Unit>>()
    val result: LiveData<Resource<Unit>> get() = _result

    init {
        getPersonalData()
    }

    private fun getPersonalData() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = personalInformationRepository.getUserPersonalDataFromDatabase()
            if (result is Resource.Error) {
                _result.postValue(Resource.Error(result.message))
            } else {
                _personalData.postValue(result.data)
            }
        }
    }

    fun updateHeight(newHeight: Int) {
        _personalData.value = _personalData.value?.copy(height = newHeight)
    }

    fun updatePhoneNumber(newNumber: Long) {
        _personalData.value = _personalData.value?.copy(phoneNumber = newNumber)
    }

    fun updateGender(newGender: String) {
        _personalData.value = _personalData.value?.copy(gender = newGender)
    }

    fun updateBirthYear(newBirthYear: Int) {
        _personalData.value = _personalData.value?.copy(birthday = newBirthYear)
    }

    fun updatePersonalData() {
        viewModelScope.launch(Dispatchers.IO) {
            _personalData.value?.let { personalData ->
                val result = personalInformationRepository.updatePersonalData(personalData)
                if (result is Resource.Success) {
                    val height = personalData.height
                    val lastWeight = personalData.weightList?.lastOrNull()?.weight

                    if (height != null && lastWeight != null) {
                        val bmi = CalculationsUtils.calculateBMI(height, lastWeight)
                        sharedPreferencesManager.saveString(SharedPreferencesManager.BMI, bmi)
                    }
                }
                _result.postValue(result)
            }
        }
    }
}
