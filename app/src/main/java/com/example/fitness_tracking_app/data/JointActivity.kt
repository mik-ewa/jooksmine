package com.example.fitness_tracking_app.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class JointActivityData(
    val data: Int = 1
) : Parcelable

@Parcelize
data class JointActivityAdapterData (
    val myPhoto: String,
    val myName: String,
    val friendsName :String,
    val friendsPhoto : String,
    val jointActivityList: List<JointActivityData>?,
): Parcelable

data class ActivitiesPagerData(
    val myPhoto: String?,
    val myName: String?,
    val friendsName :String?,
    val friendsPhoto : String?,
    val jointActivityList: List<JointActivityData>?,
    val activityList: List<FirebaseRunData>?
)