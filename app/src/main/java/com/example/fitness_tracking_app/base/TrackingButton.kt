package com.example.fitness_tracking_app.base

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.utils.TrackingConstants
import com.example.fitness_tracking_app.utils.dpToPx
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class TrackingButton(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var clTrackingButton: ConstraintLayout
    var trackingButton: ExtendedFloatingActionButton
    var stopButton: ExtendedFloatingActionButton

    init {
        inflate(context, R.layout.button_tracking, this)
        clTrackingButton = findViewById(R.id.cl_tracking_button)
        trackingButton = findViewById(R.id.bt_track)
        stopButton = findViewById(R.id.bt_stop)
    }

    private val orange = ContextCompat.getColor(context, R.color.orange)
    private val black = ContextCompat.getColor(context, R.color.black)

    private val colorAnimator by lazy {
        ValueAnimator.ofArgb(black, orange).apply {
            duration = 1000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                trackingButton.setBackgroundColor(animator.animatedValue as Int)
            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        val listener = OnClickListener { view ->
            when (view.id) {
                R.id.bt_track -> {
                    l?.onClick(view)
                }

                R.id.bt_stop -> {
                    l?.onClick(view)
                }
            }
        }
        trackingButton.setOnClickListener(listener)
        stopButton.setOnClickListener(listener)
    }

    fun setTrackingButtonStage(stage: String) {
        when (stage) {
            TrackingConstants.ACTION_START -> setStarted()
            TrackingConstants.ACTION_PAUSE -> setPaused()
            TrackingConstants.ACTION_RESUME -> setResumed()
            TrackingConstants.ACTION_NONE -> setDefault()
            TrackingConstants.ACTION_STOP -> setPaused()
        }
    }

    private fun setDefault() {
        trackingButton.text = context.getString(R.string.start)
        trackingButton.setBackgroundColor(black)
        trackingButton.translationX = 0f
        stopButton.translationX = 0f
        trackingButton.layoutParams.width = 120.dpToPx()
        trackingButton.requestLayout()
        stopButton.layoutParams.width = 60.dpToPx()
        stopButton.requestLayout()
        colorAnimator.cancel()
    }

    private fun setStarted() {
        trackingButton.text = context.getString(R.string.pause)
        colorAnimator.start()
    }

    private fun setPaused() {
        colorAnimator.pause().apply {
            trackingButton.setBackgroundColor(resources.getColor(R.color.black))
        }
        trackingButton.text = context.getString(R.string.resume)
        animateButton()
    }

    private fun setResumed() {
        trackingButton.text = context.getString(R.string.pause)
        reverseAnimateButton()
        colorAnimator.start()
    }

    private fun animateButton() {
        val moveAnimatorTracking =
            ObjectAnimator.ofFloat(trackingButton, "translationX", 40.dpToPx().toFloat())
        val moveAnimatorStop =
            ObjectAnimator.ofFloat(stopButton, "translationX", -30.dpToPx().toFloat())

        val widthAnimator = ValueAnimator.ofInt(trackingButton.width, 90.dpToPx())
        widthAnimator.addUpdateListener { valueAnimator ->
            val layoutParams = trackingButton.layoutParams
            layoutParams.width = valueAnimator.animatedValue as Int
            trackingButton.layoutParams = layoutParams
        }

        AnimatorSet().apply {
            playTogether(moveAnimatorTracking, widthAnimator, moveAnimatorStop)
            duration = 300
            start()
        }
    }

    private fun reverseAnimateButton() {
        val moveAnimatorTracking = ObjectAnimator.ofFloat(trackingButton, "translationX", 0f)
        val moveAnimatorStop = ObjectAnimator.ofFloat(stopButton, "translationX", 0f)

        val widthAnimator = ValueAnimator.ofInt(trackingButton.width, 120.dpToPx())
        widthAnimator.addUpdateListener { valueAnimator ->
            val layoutParams = trackingButton.layoutParams
            layoutParams.width = valueAnimator.animatedValue as Int
            trackingButton.layoutParams = layoutParams
        }

        AnimatorSet().apply {
            playTogether(moveAnimatorTracking, widthAnimator, moveAnimatorStop)
            duration = 300
            start()
        }
    }
}
