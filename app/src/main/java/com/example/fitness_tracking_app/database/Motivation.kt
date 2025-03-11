package com.example.fitness_tracking_app.database

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Motivation(
    val key: String?,
    val customText: String?
) : Parcelable