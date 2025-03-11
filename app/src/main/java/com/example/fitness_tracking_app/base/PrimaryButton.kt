package com.example.fitness_tracking_app.base

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieAnimationView
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.visible

class PrimaryButton(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var buttonPrimary: ConstraintLayout
    private var textTitle: TextView
    private var lottieAnimation : LottieAnimationView
    private var state: ButtonState = ButtonState.DISABLED

    init {
        inflate(context, R.layout.button_primary, this)
        textTitle = findViewById(R.id.button_text)
        buttonPrimary = findViewById(R.id.primaryButton)
        lottieAnimation = findViewById(R.id.lottie_animation)
        initAttrs(attrs)
    }

    private fun initAttrs(set: AttributeSet?) {
        set?.let {
            context.obtainStyledAttributes(it, R.styleable.CustomButton).run{
                textTitle.text = getString(R.styleable.CustomButton_text)
                textTitle.isAllCaps = getBoolean(R.styleable.CustomButton_textAllCaps, false)
                setState(getBoolean(R.styleable.CustomButton_isActive, false))
                recycle()
            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) { buttonPrimary.setOnClickListener(l) }

    fun <T : Any> setButtonState(
        dataState: DataState<T>,
    ) {
        when (dataState) {
            is DataState.Loading -> { setLoading() }
            is DataState.Error -> { setActive() }
            is DataState.Success -> {}
        }
    }

    fun setState(isActive: Boolean) {
        if (isActive) setActive() else setDisabled()
    }

    private fun setLoading() {
        state = ButtonState.LOADING
        buttonPrimary.apply {
            isClickable = false
            isEnabled = false
        }
        textTitle.gone()
        lottieAnimation.visible()
    }

    private fun setActive() {
        state = ButtonState.ACTIVE
        buttonPrimary.apply {
            setBackgroundResource(R.drawable.button_background_active)
            isClickable = true
            isEnabled = true
        }
        textTitle.visible()
        lottieAnimation.gone()
        textTitle.setTextColor(resources.getColor(R.color.white))
    }

    private fun setDisabled() {
        state = ButtonState.DISABLED
        buttonPrimary.apply {
            setBackgroundResource(R.drawable.button_background_disable)
            isClickable = false
            isEnabled = false
        }
        textTitle.visible()
        lottieAnimation.gone()
        textTitle.setTextColor(resources.getColor(R.color.black))
    }
}

private enum class ButtonState {
    LOADING,
    ACTIVE,
    DISABLED
}