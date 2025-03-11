package com.example.fitness_tracking_app.features.tracking

import android.app.PendingIntent
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import com.example.fitness_tracking_app.common.SharedPreferencesManager.Companion.BMI
import com.example.fitness_tracking_app.utils.CalculationsUtils
import com.example.fitness_tracking_app.utils.CalculationsUtils.calculateMetersToKilometers
import com.example.fitness_tracking_app.utils.CalculationsUtils.calculatePaceInMinutes
import com.example.fitness_tracking_app.utils.DateFormatter
import com.example.fitness_tracking_app.common.LocationProvider
import com.example.fitness_tracking_app.utils.NotificationHelper
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_NONE
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_PAUSE
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_RESUME
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_START
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_STOP
import com.google.android.gms.location.LocationCallback
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.IntervalSettings
import com.mapbox.common.location.LocationProviderRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject lateinit var locationProvider: LocationProvider
    @Inject lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var trackingNotification: NotificationCompat.Builder

    private var isRunning = false
    private var timeInSeconds = 0
    private var cycleNumber = 0
    private var lastPaceCalculationTime = 0
    private var distanceCovered: Double = 0.0
    private var trackingJob: Job? = null

    companion object {
        private const val MINIMUM_DISTANCE_TO_START_PACE = 20
        private const val INTERVAL_PACE_SECONDS = 15
        private const val MINIMAL_CYCLE_NUMBER_TO_TRACK = 2
        private const val UPDATE_TIME: Long = 1000
        private const val NOTIFICATION_ID = 1
    }

    private val _timeData = MutableLiveData<Int>()
    val timeData: LiveData<Int> get() = _timeData

    private val _distanceData = MutableLiveData<Double>()
    val distanceData: LiveData<Double> get() = _distanceData

    private val _paceData = MutableLiveData<Float?>()
    val paceData: LiveData<Float?> get() = _paceData

    private val _locationList = mutableListOf<android.location.Location?>()
    val locationList: List<android.location.Location?> get() = _locationList

    private val _caloriesData = MutableLiveData<Double>()
    val caloriesData: LiveData<Double> get() = _caloriesData

    inner class LocalBinder : Binder() { fun getService(): TrackingService = this@TrackingService }

    private val binder = LocalBinder()
    private var runLocationCallback: LocationCallback? = null

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()
    }

    private fun postInitialValues() {
        _timeData.postValue(0)
        _distanceData.postValue(0.0)
        _caloriesData.postValue(0.0)
        _paceData.postValue(0.0f)
    }

    private val request = LocationProviderRequest.Builder()
        .interval(IntervalSettings.Builder().interval(1000L).minimumInterval(1000L).maximumInterval(5000L).build())
        .displacement(0F)
        .accuracy(AccuracyLevel.HIGHEST)
        .build()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        notificationHelper.createNotificationChannel()
        trackingNotification = createNotification()
        startForeground(NOTIFICATION_ID, trackingNotification.build())

        when (intent?.action) {
            ACTION_STOP -> pauseTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_NONE -> stopTracking()
            ACTION_START -> {
                if (!isRunning) { postInitialValues() }
                startTrackingJob()
            }
            ACTION_RESUME -> startTrackingJob()
        }
        return START_STICKY
    }

    private fun createNotification(): NotificationCompat.Builder {
        val intent = Intent(this, TrackingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return notificationHelper.createNotification(
            smallIcon = R.drawable.icon_activities,
            title = getString(R.string.running),
            text = getString(R.string.track_your_run),
            pendingIntent = pendingIntent
        )
    }

    private fun startTrackingJob() {
        updateLocationData()
        isRunning = true
        trackingJob?.cancel()
        trackingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                if (distanceCovered >= MINIMUM_DISTANCE_TO_START_PACE && (timeInSeconds - lastPaceCalculationTime) >= INTERVAL_PACE_SECONDS) {
                    val pace = calculatePaceInMinutes(
                        seconds = timeInSeconds,
                        meters = distanceCovered
                    )
                    _paceData.postValue(pace)
                    calculateCalories()
                    lastPaceCalculationTime = timeInSeconds
                }

                updateNotification()
                delay(UPDATE_TIME)
                timeInSeconds++
                _timeData.postValue(timeInSeconds)
            }
        }
    }

    private fun calculateCalories() {
        val pace = _paceData.value ?: return
        val distance = _distanceData.value ?: return
        val bmi = sharedPreferencesManager.getString(BMI, "20").toDoubleOrNull() ?: return

        val met = getMET(pace)
        val calories = met * bmi * distance

        _caloriesData.postValue(calories)
    }

    private fun getMET(pace: Float): Double {
        return when {
            pace < 4.0 -> 16.0
            pace < 5.0 -> 12.5
            pace < 6.0 -> 10.0
            pace < 7.0 -> 8.3
            else -> 6.0
        }
    }

    private fun updateLocationData() {
        getLocationUpdates { location ->
            if (isRunning) {
                _locationList.add(location)
                if (cycleNumber >= MINIMAL_CYCLE_NUMBER_TO_TRACK) {
                    val distance = calculateDistance()
                    distanceCovered += distance
                    _distanceData.postValue(calculateMetersToKilometers(distanceCovered))
                }
                cycleNumber++
            }
        }
    }

    private fun calculateDistance(): Float {
        val locationCurrent = _locationList.last()
        val locationPrevious = _locationList[_locationList.size - 2]
        return CalculationsUtils.calculateDistance(
            lat1 = locationPrevious?.latitude!!,
            lng1 = locationPrevious.longitude,
            lat2 = locationCurrent?.latitude!!,
            lng2 = locationCurrent.longitude
        )
    }

    private fun getLocationUpdates(receivedLocation: (android.location.Location) -> Unit) {
        runLocationCallback?.let { callback ->
            locationProvider.removeLocationUpdates(callback)
            runLocationCallback = null
        }
        try {
            runLocationCallback = locationProvider.getLocationUpdates(
                request = request,
                requiredPermission = android.Manifest.permission.ACCESS_FINE_LOCATION,
                onLocationReceived = { location -> location.let(receivedLocation) })
        } catch (e: SecurityException) { /* nothing to do here */ }
    }

    private fun pauseTracking() {
        cycleNumber = 0
        isRunning = false
        trackingJob?.takeIf { it.isActive }?.cancel()
        updateNotification()
    }

    private fun updateNotification(){
        val distanceText = getString(R.string.to_distance, distanceData.value)
        notificationHelper.updateNotification(
            notificationId = NOTIFICATION_ID,
            builder = trackingNotification,
            title = if (isRunning) getString(R.string.running) else getString(R.string.paused),
            text = getString(R.string.time_distance_notification, DateFormatter.secondsToTime(timeData.value ?: 0), distanceText),
        )
    }

    private fun stopTracking() {
        isRunning = false
        trackingJob?.takeIf { it.isActive }?.cancel()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        removeCallback()
        super.onDestroy()
    }

    override fun unbindService(conn: ServiceConnection) {
        removeCallback()
        super.unbindService(conn)
    }

    private fun removeCallback() {
        runLocationCallback?.let {
            locationProvider.removeLocationUpdates(it)
            runLocationCallback = null
        }
    }
}