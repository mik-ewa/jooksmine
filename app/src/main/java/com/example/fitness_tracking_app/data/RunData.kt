package com.example.fitness_tracking_app.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class StartingData(
    val userId: String? = null,
    val startTime: Long? = null,
)

data class CompletedRunData(
    val runId : String,
    val pace: Float,
    val distance: Double,
    val duration: Int,
    val calories: Double,
    val longitude: Double,
    val latitude: Double,
    val place : String?,
    val image : String?,
) {
    companion object {
        fun createFromBaseRunData (
            baseRunData: BaseRunData,
            runId: String,
            place: String?,
            image: String?
        ) : CompletedRunData {
            return CompletedRunData(
                runId = runId,
                pace = baseRunData.pace,
                distance = baseRunData.distance,
                duration = baseRunData.duration,
                calories = baseRunData.calories,
                longitude = baseRunData.longitude,
                latitude = baseRunData.latitude,
                place = place,
                image = image
            )
        }
    }
}

data class BaseRunData(
    val pace: Float,
    val distance: Double,
    val duration: Int,
    val calories: Double,
    val longitude: Double,
    val latitude: Double
)

data class FirebaseRunData(
    val calories: Double?= null,
    val distance: Double?= null,
    val duration: Int?= null,
    val image : String?= null,
    val pace: Float?= null,
    val place : String?= null,
    val startTime: Long?= null,
    val userId: String?= null,
    val longitude: Double? = null,
    val latitude: Double? = null
)

@Parcelize
data class RunData(
    val calories: Double?,
    val distance: Double,
    val duration: Int,
    val image : String?,
    val pace: Float,
    val place : String?,
    val startTime: Long,
): Parcelable

@Parcelize
data class RunActivitiesList(
    val activitiesList: List<RunData>?,
    val userPhoto: String?
) : Parcelable {
    companion object {
        fun createFromFirebaseRunDataList(
            firebaseRunDataList: List<FirebaseRunData>?,
            userPhoto: String?
        ): RunActivitiesList {
            val runDataList = firebaseRunDataList?.map { firebase ->
                RunData(
                    calories = firebase.calories,
                    distance = firebase.distance ?: 0.0,
                    duration = firebase.duration ?: 0,
                    image = firebase.image,
                    pace = firebase.pace ?: 0f,
                    place = firebase.place,
                    startTime = firebase.startTime ?: 0L
                )
            } ?: emptyList()
            return RunActivitiesList(
                activitiesList = runDataList,
                userPhoto = userPhoto
            )
        }
    }
}