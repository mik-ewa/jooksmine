package com.example.fitness_tracking_app.base.base_components

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewbinding.ViewBinding
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.common.SharedPreferencesManager
import java.util.Locale

abstract class BaseActivity<T: ViewBinding> : AppCompatActivity() {

    private var _binding: T? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = resources.getColor(R.color.orange)
        _binding = inflateViewBinding(layoutInflater)
        setContentView(binding.root)
    }

    abstract fun inflateViewBinding(inflater: LayoutInflater): T

    override fun attachBaseContext(newBase: Context) {
        val sharedPrefs = newBase.getSharedPreferences("MY_SHARED_PREFS", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString(SharedPreferencesManager.LANGUAGE, "en") ?: "en"
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = newBase.resources.configuration.apply { setLocale(locale) }
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    fun nextFragment(
        fragment: Fragment,
        addToBackStack: Boolean = true,
        dataBundle: Bundle? = null,
        hasAnimation: Boolean = true,
    ) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.placeholder)
        if (currentFragment!= null && currentFragment::class == fragment::class) { return }

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()

        if (addToBackStack) { ft.addToBackStack(null) }

        if (hasAnimation) {
            ft.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        fragment.arguments = dataBundle

        ft.replace(R.id.placeholder, fragment)

        ft.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
