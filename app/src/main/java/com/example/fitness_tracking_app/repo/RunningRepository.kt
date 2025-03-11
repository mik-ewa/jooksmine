package com.example.fitness_tracking_app.repo

import com.example.fitness_tracking_app.BuildConfig
import com.example.fitness_tracking_app.data.CompletedRunData
import com.example.fitness_tracking_app.data.FirebaseRunData
import com.example.fitness_tracking_app.data.StartingData
import com.example.fitness_tracking_app.common.FirebasePaths
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.utils.RepositoryUtils.safeResource
import com.example.fitness_tracking_app.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID

class RunningRepository (
    private val firebaseDb: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
):  IRunningRepository {

    private val currentUser : FirebaseUser
        get() = firebaseAuth.currentUser ?: throw Exception(GlobalStrings.ERROR_USER_NOT_AUTHENTICATED)

    private val runningReference: DatabaseReference
        get() = firebaseDb.getReference(FirebasePaths.RUNNING)

    override suspend fun startRunning(startDate: Long): Resource<String> = safeResource {
        val startingData = StartingData(userId = currentUser.uid, startTime = startDate)
        val runId = UUID.randomUUID().toString()
        runningReference.child(runId).setValue(startingData).await()
        Resource.Success(runId)
    }

    override suspend fun getUserRuns(runList: List<String>?): List<FirebaseRunData>? {
        return try {
            val runsList: MutableList<FirebaseRunData> = mutableListOf()
            for (run in runList!!) {
                run?.let {
                    val runsData = runningReference.child(run).get().await().getValue<FirebaseRunData>()
                    if (runsData != null) { runsList.add(runsData) }
                }
            }
            runsList.toList()
        } catch (e: Exception) {
            Timber.d("Exception: $e")
            null
        }
    }

    override suspend fun finishRunning(completedRunData : CompletedRunData): Resource<Unit> = safeResource {
        val updates = mapOf(
            FirebasePaths.PACE to completedRunData.pace,
            FirebasePaths.DISTANCE to completedRunData.distance,
            FirebasePaths.DURATION to completedRunData.duration,
            FirebasePaths.IMAGE to completedRunData.image,
            FirebasePaths.PLACE to completedRunData.place,
            FirebasePaths.CALORIES to completedRunData.calories,
            FirebasePaths.LONGITUDE to completedRunData.longitude,
            FirebasePaths.LATITUDE to completedRunData.latitude
        )
        runningReference.child(completedRunData.runId).updateChildren(updates).await()
        Resource.Success(Unit)
    }

    override suspend fun removeRunningData(runId: String) {
        try {
            runningReference.child(runId).removeValue().await()
        } catch (e: Exception) {
            // save in cache
        }
    }
}