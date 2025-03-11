package com.example.fitness_tracking_app.features.activity_splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.example.fitness_tracking_app.base.base_components.BaseActivity
import com.example.fitness_tracking_app.databinding.ActivitySplashBinding
import com.example.fitness_tracking_app.features.activity_login.LoginActivity
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    override fun inflateViewBinding(inflater: LayoutInflater) =
        ActivitySplashBinding.inflate(layoutInflater)

    private fun initViewModel() {
        viewModel.currentUser.observe(this@SplashActivity) {
            if (it == false) { goToLoginActivity() } else { goToMainActivity() }
        }
    }

    private fun goToLoginActivity() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun goToMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}