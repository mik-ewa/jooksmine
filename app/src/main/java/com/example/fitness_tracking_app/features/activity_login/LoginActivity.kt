package com.example.fitness_tracking_app.features.activity_login

import android.os.Bundle
import android.view.LayoutInflater
import com.example.fitness_tracking_app.base.base_components.BaseActivity
import com.example.fitness_tracking_app.databinding.ActivityLoginBinding
import com.example.fitness_tracking_app.features.login.LoginFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nextFragment(LoginFragment())
    }

    override fun inflateViewBinding(inflater: LayoutInflater) = ActivityLoginBinding.inflate(layoutInflater)
}
