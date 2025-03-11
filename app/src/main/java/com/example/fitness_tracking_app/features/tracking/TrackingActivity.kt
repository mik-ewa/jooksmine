package com.example.fitness_tracking_app.features.tracking

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF

import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.common.DialogManager
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import com.example.fitness_tracking_app.common.SharedPreferencesManager.Companion.TRACKING
import com.example.fitness_tracking_app.data.BaseRunData
import com.example.fitness_tracking_app.databinding.ActivityTrackingBinding
import com.example.fitness_tracking_app.utils.CalculationsUtils.formatPace
import com.example.fitness_tracking_app.utils.DateFormatter
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.data.LocationType
import com.example.fitness_tracking_app.common.NetworkTracker
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_NONE
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_STOP
import com.example.fitness_tracking_app.utils.setGone
import com.example.fitness_tracking_app.utils.showItemContainer
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapSnapshotOptions
import com.mapbox.maps.Size
import com.mapbox.maps.SnapshotStyleListener
import com.mapbox.maps.Snapshotter
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackingActivity : AppCompatActivity(), SnapshotStyleListener {

    @Inject
    lateinit var networkTracker: NetworkTracker
    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager
    @Inject
    lateinit var dialogManager: DialogManager

    private val binding by lazy { ActivityTrackingBinding.inflate(layoutInflater) }

    private val viewModel: TrackingViewModel by viewModels()

    private var trackingService: TrackingService? = null

    private var isBound = false

    private lateinit var mapSnapshotter: Snapshotter

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.navigationBarColor = resources.getColor(R.color.grey_150)
        binding.tvClose.setOnClickListener { finish() }

        locationPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        initView()
                    }

                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        setOnFbTrackClick(LocationType.FINE_LOCATION)
                    }

                    else -> {
                        setOnFbTrackClick(LocationType.LOCATION)
                    }
                }
            }
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        setRunningStatus(sharedPreferencesManager.getString(TRACKING, ACTION_NONE))
        requestLocationPermissions()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? TrackingService.LocalBinder
            trackingService = binder?.getService()
            isBound = true
            trackingService?.let { serviceTracking ->
                serviceTracking.timeData.observe(this@TrackingActivity) { time ->
                    binding.tvDuration.text = DateFormatter.secondsToTime(time)
                }
                serviceTracking.distanceData.observe(this@TrackingActivity) { distance ->
                    binding.tvDistance.text = getString(R.string.to_distance, distance)
                }
                serviceTracking.paceData.observe(this@TrackingActivity) { pace ->
                    binding.tvPace.text = pace?.let { formatPace(it) }
                }
                serviceTracking.caloriesData.observe(this@TrackingActivity) { kcal ->
                    binding.tvKcal.text = getString(R.string.to_kcal, kcal)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            isBound = false
        }
    }

    private fun initViewModel() {
        viewModel.runResult.observe(this@TrackingActivity) {
            if (it is Resource.Error) {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.runStatus.observe(this@TrackingActivity) { status -> setRunningStatus(status) }
    }

    private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initView()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun setCurrentStatusContent(value: String) {
        val isActionNone = value == ACTION_NONE
        val isActionStop = value == ACTION_STOP
        binding.apply {
            llDuration.setGone(isActionNone)
            llPace.setGone(isActionNone)
            if (isActionNone) {
                llTopData.showItemContainer(0)
            } else {
                llTopData.showItemContainer(40)
            }
            if (isActionStop) {
                showFinishRunDialog(getBaseRunData())
            }
        }
    }

    private fun initView() {
        binding.apply {
            initMap()
            fbTrack.trackingButton.setOnClickListener { viewModel.toggleTrackingStatus() }
            fbTrack.stopButton.setOnClickListener { viewModel.toggleTrackingStatus(buttonType = ButtonType.STOP) }
        }
    }

    private fun getBaseRunData(): BaseRunData? {
        trackingService?.let { service ->
            val baseRunData = BaseRunData(
                pace = service.paceData.value ?: 0.0f,
                distance = service.distanceData.value ?: 0.00,
                duration = service.timeData.value ?: 0,
                longitude = service.locationList.first()?.longitude ?: 0.0,
                latitude = service.locationList.first()?.latitude ?: 0.0,
                calories = service.caloriesData.value ?: 0.0
            )
            sharedPreferencesManager.saveRunBaseData(baseRunData)
        }
        return sharedPreferencesManager.getRunBaseData()
    }

    private fun showFinishRunDialog(baseRunData: BaseRunData?) {
        baseRunData?.let {
            dialogManager.showFinishRunDialog(
                baseRunData = baseRunData,
                onResumeClick = {
                    viewModel.toggleTrackingStatus()
                    sharedPreferencesManager.clearSharedPrefsSpecificData(SharedPreferencesManager.BASE_RUN_DATA)
                },
                onFinishAndSaveClick = {
                    takeSnapshot(latitude = baseRunData.latitude, longitude = baseRunData.longitude) { bitmap ->
                        viewModel.insertRunData(
                            baseRunData = baseRunData,
                            bitmap = bitmap,
                        )
                    }
                    finishAndClear()
                },
                onFinishAndDeleteClick = {
                    viewModel.deleteRunData()
                    finishAndClear()
                }
            )
        } ?: Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
    }

    private fun finishAndClear() {
        viewModel.toggleTrackingStatus(buttonType = ButtonType.STOP)
        unbindService(serviceConnection)
        sharedPreferencesManager.clearSharedPrefsSpecificData(SharedPreferencesManager.BASE_RUN_DATA)
    }

    private fun bindService() {
        Intent(this@TrackingActivity, TrackingService::class.java).also { intent ->
            bindService(
                intent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun initMap() {
        binding.mapView.apply {
            location.locationPuck = createDefault2DPuck(withBearing = true)
            location.enabled = true
            location.puckBearing = com.mapbox.maps.plugin.PuckBearing.HEADING
            location.puckBearingEnabled = true
            viewport.transitionTo(
                targetState = viewport.makeFollowPuckViewportState(),
                transition = viewport.makeImmediateViewportTransition()
            )
        }
    }

    private fun needsPermissions() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts(GlobalStrings.PACKAGE, packageName, null)
        }
        startActivity(intent)
    }

    private fun setRunningStatus(trackingStatus: String) {
        if (trackingStatus != ACTION_NONE) {
            bindService()
        }
        setCurrentStatusContent(trackingStatus)
        setTrackingStatusToService(trackingStatus)
        binding.fbTrack.setTrackingButtonStage(trackingStatus)
        sharedPreferencesManager.saveString(TRACKING, trackingStatus)
    }

    private fun setTrackingStatusToService(status: String) {
        val intent = Intent(this, TrackingService::class.java).apply { action = status }
        this@TrackingActivity.startService(intent)
    }

    private fun setOnFbTrackClick(type: LocationType) {
        binding.fbTrack.setOnClickListener {
            dialogManager.showLocationNotificationDialog(
                type = type,
                onButtonClick = { needsPermissions() })
        }
    }

    private fun takeSnapshot(longitude: Double, latitude: Double, callback: (Bitmap?) -> Unit) {
        val snapshotterOptions = MapSnapshotOptions.Builder()
            .size(Size(900.0f, 600.0f))
            .pixelRatio(1.0f)
            .build()

        mapSnapshotter = Snapshotter(this, snapshotterOptions).apply {
            setStyleListener(this@TrackingActivity)
            setStyleUri(Style.STANDARD)
            setCamera(
                CameraOptions.Builder().zoom(13.0).center(Point.fromLngLat(longitude, latitude)).build()
            )
            start(
                overlayCallback = { overlay ->
                    overlay.canvas.drawOval(
                        RectF(0f, 0f, 100f, 100f),
                        Paint().apply { alpha = 128 })
                }
            ) { bitmap, errorMessage ->
                if (errorMessage != null) {
                    Toast.makeText(this@TrackingActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
                callback(bitmap)
            }
        }
    }

    override fun onDidFinishLoadingStyle(style: Style) {}
}