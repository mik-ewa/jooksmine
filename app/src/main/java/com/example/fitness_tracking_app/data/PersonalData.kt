package com.example.fitness_tracking_app.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class PersonalData (
    val gender: String? = null,
    val phoneNumber: Long? = null,
    val birthday: Int? = null,
    val height: Int? = null,
    val weightList: List<Weights>? = null
)

@Parcelize
data class Weights(
    val date: Long? = null,
    val weight: Double? = null,
) : Parcelable