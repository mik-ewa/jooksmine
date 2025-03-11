package com.example.fitness_tracking_app.repo

import com.example.fitness_tracking_app.BuildConfig
import com.example.fitness_tracking_app.data.LanguageData
import com.example.fitness_tracking_app.data.LanguagesResponse
import com.example.fitness_tracking_app.data.PersonalData
import com.example.fitness_tracking_app.data.Weights
import com.example.fitness_tracking_app.database.dao.UserDao
import com.example.fitness_tracking_app.database.Motivation
import com.example.fitness_tracking_app.common.FirebasePaths
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.utils.RepositoryUtils.safeResource
import com.example.fitness_tracking_app.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PersonalInformationRepository(
    private val firebaseDb: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userDao: UserDao
) : IPersonalInformationRepository{

    private val currentUser : FirebaseUser
        get() = firebaseAuth.currentUser ?: throw Exception(GlobalStrings.ERROR_USER_NOT_AUTHENTICATED)

    private val userReference: DatabaseReference
        get()= firebaseDb.getReference(FirebasePaths.USER)

    private val personalInformationRef: DatabaseReference
        get() = userReference.child(firebaseAuth.uid!!).child(FirebasePaths.USER_DATA).child(
            FirebasePaths.PERSONAL_DATA)

    override suspend fun getUser() = userDao.getUser(currentUser.uid)
    override fun getUserFlow() = userDao.getUserFlow(currentUser.uid)

    override suspend fun saveMotivation(motivation: Motivation) = userDao.updateMotivation(currentUser.uid, motivation)

    override suspend fun getUserPersonalDataFromDatabase(): Resource<PersonalData?> = safeResource {
        val snapshot = suspendCoroutine { continuation ->
            userReference.child(currentUser.uid).child(FirebasePaths.USER_DATA)
                .child(FirebasePaths.PERSONAL_DATA)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
        val personalData = snapshot.getValue<PersonalData>()
        Resource.Success(personalData)
    }

    override suspend fun updatePersonalData(personalData: PersonalData): Resource<Unit> = safeResource {
        personalInformationRef.setValue(personalData).await()
        Resource.Success(Unit)
    }

    override suspend fun deleteWeight(weight: Weights) {
        try {
            val weightList = personalInformationRef.child(FirebasePaths.WEIGHT_LIST).get().await()
                .getValue<List<Weights>?>() ?: emptyList()
            val updatedList = weightList.filterNot { it == weight }
            personalInformationRef.child(FirebasePaths.WEIGHT_LIST).setValue(updatedList).await()
        } catch (e: Exception) {
            Timber.e(e.message)
        }
    }

    override suspend fun addWeight(weight: Weights) {
        try {
            val weightList = personalInformationRef.child(FirebasePaths.WEIGHT_LIST).get().await()
                .getValue<List<Weights>?>() ?: emptyList()
            val updatedList = weightList.toMutableList()
            updatedList.add(weight)
            personalInformationRef.child(FirebasePaths.WEIGHT_LIST).setValue(updatedList).await()
        } catch (e: Exception) {
            Timber.e(e.message)
        }
    }

    override fun getLanguages() =
        LanguagesResponse(
            listOf(
                LanguageData(
                    language = "Polski",
                    flag = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/12/Flag_of_Poland.svg/1280px-Flag_of_Poland.svg.png",
                    code = "pl"
                ),
                LanguageData(
                    language = "English",
                    flag = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Flag_of_the_United_Kingdom_%283-5%29.svg/1600px-Flag_of_the_United_Kingdom_%283-5%29.svg.png",
                    code = "en"
                ),
                LanguageData(
                    language = "Deutsch",
                    flag = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/Flag_of_Germany.svg/1600px-Flag_of_Germany.svg.png",
                    code = "de"
                )
            )
        )

}
