package com.example.fitness_tracking_app.features.activity_main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import com.example.fitness_tracking_app.base.base_components.BaseActivity
import com.example.fitness_tracking_app.common.SharedPreferencesManager.Companion.TRACKING
import com.example.fitness_tracking_app.databinding.ActivityMainBinding
import com.example.fitness_tracking_app.features.SharedChatViewModel
import com.example.fitness_tracking_app.features.SharedHomeViewModel
import com.example.fitness_tracking_app.features.about.AboutFragment
import com.example.fitness_tracking_app.features.activity_splash.SplashActivity
import com.example.fitness_tracking_app.features.tracking.TrackingActivity
import com.example.fitness_tracking_app.features.chat.ChatFragment
import com.example.fitness_tracking_app.features.complete_sign_up.CompleteSignUpFragment
import com.example.fitness_tracking_app.features.home.HomeFragment
import com.example.fitness_tracking_app.features.progress.ProgressFragment
import com.example.fitness_tracking_app.features.start_tracking.StartNewRecordBottomSheet
import com.example.fitness_tracking_app.repo.AuthManager
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.common.LocationProvider
import com.example.fitness_tracking_app.common.NetworkTracker
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.TrackingConstants.ACTION_NONE
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.setVisible
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    @Inject lateinit var networkTracker: NetworkTracker
    @Inject lateinit var locationProvider: LocationProvider
    @Inject lateinit var sharedPreferencesManager: SharedPreferencesManager

    private val viewModel: MainViewModel by viewModels()
    private val sharedHomeViewModel: SharedHomeViewModel by viewModels()
    private val sharedChatViewModel: SharedChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBottomNavBar(false)
        fetchWeather()
        bottomNav()
        initViewModel()
    }

    override fun onStart() {
        super.onStart()
        showInternetLabel()
        viewModel.setMyActiveStatus(true)
        AuthManager.addAuthStateObserver { user -> if (user == null) { handleUserNotAuthenticated() } }
    }

    override fun inflateViewBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(layoutInflater)

    fun fetchWeather() { sharedHomeViewModel.fetchWeather() }

    private fun initViewModel() {
        viewModel.ifUserExists.observe(this@MainActivity) {
            when (it) {
                is Resource.Success -> {
                    binding.flLoading.gone()
                    handleUserStatus(it.data)
                }

                is Resource.Error -> {
                    binding.flLoading.gone()
                    errorUserAction(it.message ?: GlobalStrings.ERROR_UNKNOWN)
                }

                is Resource.Loading -> {
                    binding.flLoading.visible()
                }
            }
        }
        sharedChatViewModel.unreadChatsNumber.observe(this@MainActivity) { number ->
            val bottomBadge = binding.bottomNavigationView.getOrCreateBadge(R.id.chat)
            if (number > 0) {
                bottomBadge.isVisible = true
                bottomBadge.backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.orange50)
                bottomBadge.number = number
            } else {
                bottomBadge.isVisible = false
            }
        }
    }

    private fun bottomNav() {
        binding.apply {
            fbRecord.setOnClickListener {
                if (isTrackingServiceRunning()) {
                    val intent = Intent(this@MainActivity, TrackingActivity::class.java)
                    startActivity(intent)
                } else if (!networkTracker.isConnectedToInternet()) {
                    showNoInternetDialog()
                } else {
                    val bottomSheetFragment = StartNewRecordBottomSheet()
                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                }
            }

            bottomNavigationView.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.home -> {
                        nextFragment(HomeFragment(), addToBackStack = false, hasAnimation = false)
                        true
                    }

                    R.id.chat -> {
                        nextFragment(ChatFragment(), addToBackStack = false, hasAnimation = false)
                        true
                    }

                    R.id.progress -> {
                        nextFragment(ProgressFragment(), addToBackStack = false, hasAnimation = false)
                        true
                    }

                    R.id.about -> {
                        nextFragment(AboutFragment(), addToBackStack = false, hasAnimation = false)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun handleUserStatus(userStatus: Boolean?) {
        userStatus?.let { userActiveStatus ->
            if (userActiveStatus) nextFragment(HomeFragment(), hasAnimation = false)
            else errorUserAction(GlobalStrings.ERROR_USER_ACCOUNT_DELETED)
        } ?: nextFragment(CompleteSignUpFragment(), addToBackStack = false, hasAnimation = false)
    }

    private fun errorUserAction(error: String) {
        signOut()
        Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
    }

    private fun showNoInternetDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_internet_connection_title))
            .setMessage(getString(R.string.no_internet_connection_text))
            .setPositiveButton(getString(R.string.ok), null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(ContextCompat.getColor(this, R.color.orange150))
    }

    private fun isTrackingServiceRunning(): Boolean {
        val trackingData = sharedPreferencesManager.getString(TRACKING, ACTION_NONE)
        return trackingData != ACTION_NONE
    }

    private fun showInternetLabel() {
        networkTracker.registerNetworkCallback { isConnected ->
            if (isConnected) { runOnUiThread { binding.labelNoConnection.gone() } }
            else { runOnUiThread { binding.labelNoConnection.visible() } }
        }
    }

    fun showBottomNavBar(shouldBeVisible: Boolean) {
        binding.fbRecord.setVisible(shouldBeVisible)
        binding.bottomAppbar.setVisible(shouldBeVisible)
    }

    private fun handleUserNotAuthenticated() {
        Toast.makeText(this, getString(R.string.session_expired), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        this.finishAffinity()
    }

    override fun onStop() {
        super.onStop()
        networkTracker.unregisterNetworkCallback()
        viewModel.setMyActiveStatus(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        AuthManager.removeAuthStateObserver()
    }

    fun signOut() { viewModel.signOut() }
}
