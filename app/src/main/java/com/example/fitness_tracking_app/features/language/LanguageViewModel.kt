package com.example.fitness_tracking_app.features.language

import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import com.example.fitness_tracking_app.common.SharedPreferencesManager.Companion.LANGUAGE
import com.example.fitness_tracking_app.data.LanguageData
import com.example.fitness_tracking_app.data.LanguagesResponse
import com.example.fitness_tracking_app.repo.PersonalInformationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val personalInformationRepository: PersonalInformationRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    private val _languages = MutableLiveData<List<LanguageData>>()
    val languages: LiveData<List<LanguageData>>get() = _languages

    private var languageResponse : LanguagesResponse? = null

    init { getLanguages() }

    private fun getLanguages() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = personalInformationRepository.getLanguages()
            val languageCode = sharedPreferencesManager.getString(LANGUAGE, "en")
            languageResponse = response
            changeLanguage(languageCode)
        }
    }

    fun changeLanguage(code: String) {
        val languages = languageResponse?.languages?.map {
            it.isSelected = it.code.contains(code)
            it
        }
        _languages.postValue(languages ?: emptyList())
    }

    fun setLanguage(language: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferencesManager.saveString(LANGUAGE, language)
        }
    }
}