package com.example.fitness_tracking_app.common

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.data.BaseRunData
import com.example.fitness_tracking_app.data.Weights
import com.example.fitness_tracking_app.utils.CalculationsUtils.formatPace
import com.example.fitness_tracking_app.utils.DateFormatter
import com.example.fitness_tracking_app.data.LocationType
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.isWeightValid
import com.example.fitness_tracking_app.utils.visible
import javax.inject.Inject

class DialogManager @Inject constructor(private val activity: Activity) {

    fun showFinishRunDialog(
        baseRunData: BaseRunData,
        onResumeClick: () -> Unit,
        onFinishAndSaveClick: () -> Unit,
        onFinishAndDeleteClick: () -> Unit
    ) {
        Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_finish_run)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            if (baseRunData.pace > 0.0f) {
                val llStats: LinearLayout = findViewById(R.id.ll_run_stats)
                val finishAndSave: Button = findViewById(R.id.bg_finish_save)
                val durationText: TextView = findViewById(R.id.tv_duration)
                val paceText: TextView = findViewById(R.id.tv_pace)
                val distanceText: TextView = findViewById(R.id.tv_distance)
                val caloriesText: TextView = findViewById(R.id.tv_calories)
                finishAndSave.visible()
                llStats.visible()

                finishAndSave.setOnClickListener {
                    onFinishAndSaveClick.invoke()
                    dismiss()
                }

                caloriesText.text = context.getString(R.string.to_kcal, baseRunData.calories)
                distanceText.text = context.getString(R.string.to_distance, baseRunData.distance)
                paceText.text = formatPace(baseRunData.pace)
                durationText.text = DateFormatter.secondsToTime(baseRunData.duration)
            }

            val finishAndDelete: Button = findViewById(R.id.bg_finish_delete)
            val resumeButton: Button = findViewById(R.id.bg_resume)
            resumeButton.setOnClickListener {
                onResumeClick.invoke()
                dismiss()
            }
            finishAndDelete.setOnClickListener {
                onFinishAndDeleteClick.invoke()
                dismiss()
            }
        }.show()
    }

    fun showSingleActionDialog(
        textQuestion: String,
        buttonText: String,
        onButtonClick: () -> Unit
    ) {
        Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_single_action)
            setCancelable(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val closeButton: ImageView? = findViewById(R.id.icon_close)
            val questionText: TextView = findViewById(R.id.tv_question)
            val button: Button = findViewById(R.id.button_remove_record)
            closeButton?.setOnClickListener {
                dismiss()
            }
            questionText.text = textQuestion
            button.text = buttonText
            button.setOnClickListener {
                onButtonClick.invoke()
                dismiss()
            }
            show()
        }
    }

    fun showMotivationDialog(
        onButtonClick: (Any) -> Unit
    ) {
        Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_motivation)
            setCancelable(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val closeButton: ImageView? = findViewById(R.id.icon_close)
            val buttonSave: Button = findViewById(R.id.button_save)
            val llMotivation: LinearLayout = findViewById(R.id.ll_motivation)
            val motivationText: EditText = findViewById(R.id.et_motivation)
            val meterText: TextView = findViewById(R.id.tv_meter)

            llMotivation.visible()
            meterText.text = context.getString(R.string.meter_motivation, 0)

            motivationText.addTextChangedListener {
                val letterNumbers = motivationText.text.count()
                meterText.text = context.getString(R.string.meter_motivation, letterNumbers)
            }

            buttonSave.setOnClickListener {
                onButtonClick.invoke(motivationText.text.toString())
                dismiss()
            }
            closeButton?.setOnClickListener {
                dismiss()
            }
            show()
        }
    }

    fun showLocationNotificationDialog(
        type: LocationType,
        onButtonClick: () -> Unit
    ) {
        Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_notification)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val buttonSettings: Button? = findViewById(R.id.button_settings)
            val buttonBack: Button? = findViewById(R.id.button_back)
            val tvInfo: TextView? = findViewById(R.id.tv_info)

            tvInfo?.text = when (type) {
                LocationType.LOCATION -> context.getString(R.string.location_permissions_are_disabled)
                LocationType.FINE_LOCATION -> context.getString(R.string.fine_location_permissions_are_disabled)
            }
            buttonBack?.setOnClickListener {
                dismiss()
            }
            buttonSettings?.setOnClickListener {
                onButtonClick.invoke()
                dismiss()
            }
            show()
        }
    }

    fun showMeasurementsDialog(onClick: (Weights) -> Unit) {
        Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_measurement)
            setCancelable(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val closeButton: ImageView? = findViewById(R.id.icon_close)
            val buttonSave: Button = findViewById(R.id.button_save)
            val etWeight: EditText = findViewById(R.id.et_weight)
            val errorTextWeight: TextView = findViewById(R.id.tv_error_weight)

            buttonSave.setOnClickListener {
                errorTextWeight.gone()
                if (etWeight.isWeightValid()) {
                    val weight = Weights(
                        date = System.currentTimeMillis(),
                        weight = etWeight.text.toString().toDouble()
                    )
                    onClick.invoke(weight)
                    dismiss()
                } else {
                    errorTextWeight.visible()
                }
            }

            closeButton?.setOnClickListener {
                dismiss()
            }
            show()
        }
    }
}
