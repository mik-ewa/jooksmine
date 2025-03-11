package com.example.fitness_tracking_app.features

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import com.example.fitness_tracking_app.features.home.HomeRecyclerItem
import com.example.fitness_tracking_app.weatherMockInfo
import com.example.fitness_tracking_app.repo.WeatherRepository
import com.example.fitness_tracking_app.utils.DateFormatter.timeMillisToDateTime
import com.example.fitness_tracking_app.common.LocationHelper
import com.example.fitness_tracking_app.common.LocationProvider
import com.example.fitness_tracking_app.common.NetworkTracker
import com.google.android.gms.location.LocationCallback
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.IntervalSettings
import com.mapbox.common.location.LocationProviderRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedHomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationHelper: LocationHelper,
    private val networkTracker: NetworkTracker,
    private val locationProvider: LocationProvider,
    private val sharedPreferencesManager: SharedPreferencesManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _weatherInfo = MutableLiveData<HomeRecyclerItem.WeatherItem?>()
    val weatherInfo: LiveData<HomeRecyclerItem.WeatherItem?> get() = _weatherInfo

    private var weatherLocationCallback: LocationCallback? = null

    private fun fetchCityName(location: Location): String? { return locationHelper.getCityName(context, latitude = location.latitude, longitude = location.longitude) }

    fun fetchWeather() {
        weatherLocationCallback?.let { callback ->
            locationProvider.removeLocationUpdates(callback)
            weatherLocationCallback = null
        }

        try {
            val request = LocationProviderRequest.Builder()
                .interval(IntervalSettings.Builder().interval(1800000L).minimumInterval(1800000L).maximumInterval(3600000L).build())
                .displacement(0F)
                .accuracy(AccuracyLevel.LOW)
                .build()

            weatherLocationCallback = locationProvider.getLocationUpdates(
                request = request,
                requiredPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION,
                onLocationReceived = { location -> getWeatherData(location) }
            )
        } catch (e: SecurityException) {  }
    }

    private fun getWeatherData(location: Location) {
        if (!networkTracker.isConnectedToInternet()) {
            _weatherInfo.postValue(sharedPreferencesManager.getWeatherData())
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cityName = fetchCityName(location)
                val weatherResponse = weatherRepository.getCurrentWeather(
                    long = location.longitude,
                    lat = location.latitude,
                    time = System.currentTimeMillis() / 1000
                )
                val weatherDataInfo =
                    if (weatherResponse.isSuccessful && weatherResponse.body() != null) {
                        val weatherData = HomeRecyclerItem.WeatherItem.createFromWeatherResponse(weatherResponse.body()!!, timeMillisToDateTime(System.currentTimeMillis()))
                        if (cityName != null) { weatherData.copy(city = cityName) } else { weatherData }
                    } else if (weatherResponse.code() == 401) {
                        weatherMockInfo
                    } else {
                        null
                    }
                weatherDataInfo?.let { weather -> sharedPreferencesManager.saveWeatherData(weather) }
                _weatherInfo.postValue(weatherDataInfo)
            } catch (e: Exception) { }
        }
    }

    override fun onCleared() {
        weatherLocationCallback?.let { callback ->
            locationProvider.removeLocationUpdates(callback)
            weatherLocationCallback = null
        }
        super.onCleared()
    }
}