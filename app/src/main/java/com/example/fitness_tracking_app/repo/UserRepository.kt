package com.example.fitness_tracking_app.repo

import android.net.Uri
import com.example.fitness_tracking_app.BuildConfig
import com.example.fitness_tracking_app.data.JointActivityData
import com.example.fitness_tracking_app.data.UserAccountData
import com.example.fitness_tracking_app.data.UserData
import com.example.fitness_tracking_app.data.UserLoginData
import com.example.fitness_tracking_app.database.dao.UserDao
import com.example.fitness_tracking_app.database.entity.User
import com.example.fitness_tracking_app.common.FirebasePaths
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.utils.RepositoryUtils.safeFirebaseCall
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserRepository(
    private val firebaseDb: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userDao: UserDao
) : IUserRepository {

    private val currentUser : FirebaseUser
        get() = firebaseAuth.currentUser ?: throw Exception(GlobalStrings.ERROR_USER_NOT_AUTHENTICATED)

    private val userReference: DatabaseReference get()= firebaseDb.getReference(FirebasePaths.USER)

    private val activeReference: DatabaseReference get() = firebaseDb.getReference(FirebasePaths.ACTIVE_STATUS)

    private val usernameReference: DatabaseReference get() = firebaseDb.getReference(FirebasePaths.USERNAME)

    init { userReference.child(currentUser.uid).keepSynced(true) }

    private val storageRef: FirebaseStorage get() = FirebaseStorage.getInstance()

    val isMainUser: Boolean get() = firebaseAuth.currentUser?.uid == BuildConfig.MAIN_FIREBASE_USER

    override suspend fun getUserFromDao() = userDao.getUser(currentUser.uid)

    override fun getUserFlowFromDao() = userDao.getUserFlow(currentUser.uid)

    override suspend fun ifUserExists(): Resource<Boolean?> = safeResource {
        val snapshot = suspendCoroutine { continuation ->
            userReference.child(currentUser.uid).child(FirebasePaths.ACTIVE_ACCOUNT)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }

        val userStatus = snapshot.getValue<Boolean?>()
        Resource.Success(userStatus)
    }

    override suspend fun getUserFromDatabase(): Resource<UserData?> = safeResource {
        val snapshot = suspendCoroutine { continuation ->
            userReference.child(currentUser.uid).child(FirebasePaths.USER_DATA)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }

        val userData = snapshot.getValue<UserData>()
        Resource.Success(userData)
    }

    override suspend fun setStatus(isActive: Boolean) {
        try {
            activeReference.child(currentUser.uid).setValue(isActive).await()
        } catch (e: Exception) {
            Timber.e(e.message)
        }
    }

    override suspend fun fetchJointActivity(friendId: String): Resource<List<JointActivityData>?> =
        safeResource {
            //to be implemented
            val jointActivityList = emptyList<JointActivityData>()
            Resource.Success(jointActivityList)
        }

    override suspend fun checkUsername(username: String): Resource<Boolean> {
        val query = usernameReference.child(username)
        return CompletableDeferred<Resource<Boolean>>().also { deferred ->
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    deferred.complete(Resource.Success(snapshot.exists()))
                }

                override fun onCancelled(error: DatabaseError) {
                    deferred.complete(Resource.Error(error.message))
                }
            })
        }.await()
    }

    override suspend fun saveUser(userLoginData: UserLoginData): Resource<String> =
        safeFirebaseCall(
            firebaseAction = {
                val firebaseUser = firebaseAuth.currentUser
                    ?: throw Exception(GlobalStrings.ERROR_USER_NOT_AUTHENTICATED)

                val placeholderProfileList =
                    storageRef.reference.child(FirebasePaths.PROFILE_PLACEHOLDER_PHOTO).listAll().await()
                val placeholderBackgroundList =
                    storageRef.reference.child(FirebasePaths.BACKGROUND_PLACEHOLDER_PHOTO).listAll().await()
                val placeholderProfilePhoto: String =
                    placeholderProfileList.items.randomOrNull()?.downloadUrl?.await()?.toString().toString()
                val placeholderBackgroundPhoto: String =
                    placeholderBackgroundList.items.randomOrNull()?.downloadUrl?.await()?.toString().toString()

                val userAccountData = UserAccountData.createFromLoginData(
                    userLoginData,
                    uid = firebaseUser.uid,
                    email = firebaseUser.email!!,
                    profilePhoto = placeholderProfilePhoto,
                    backgroundPhoto = placeholderBackgroundPhoto
                )

                userReference.child(firebaseUser.uid).setValue(userAccountData).await()
                userLoginData.username.lowercase()
            },
            onSuccessAction = { username ->
                saveUsername(username)
            }
        )

    private suspend fun saveUsername(username: String): Resource<Unit> = safeResource {
        usernameReference.child(username).setValue(currentUser.uid).await()
        Resource.Success(Unit)
    }

    override suspend fun saveUserToDao(userData: UserData) {
        userDao.insertUser(
            User(
                uid = currentUser.uid,
                name = userData.name,
                username = userData.username,
                profilePhoto = userData.profilePhoto,
            )
        )
    }

    override suspend fun saveProfilePhoto(picture: Uri): Resource<String> = safeFirebaseCall(
        firebaseAction = {
            val storageReference = storageRef.reference.child(FirebasePaths.PROFILE_PHOTO).child(currentUser.uid)
            storageReference.putFile(picture).await()
            val downloadUrl = storageReference.downloadUrl.await()
            userReference.child(currentUser.uid).child(FirebasePaths.USER_DATA).child(FirebasePaths.PROFILE_PHOTO).setValue(downloadUrl.toString())

            downloadUrl.toString()
        }, onSuccessAction = { photo ->
            userDao.updateProfilePhoto(currentUser.uid, photo)
        }
    )

    override suspend fun saveBackgroundPhoto(picture: Uri): Resource<String> = safeResource {
        val storageReference = storageRef.reference.child(FirebasePaths.BACKGROUND_PHOTO).child(currentUser.uid)
        storageReference.putFile(picture).await()
        val downloadUrl = storageReference.downloadUrl.await()
        userReference.child(currentUser.uid).child(FirebasePaths.USER_DATA).child(FirebasePaths.BACKGROUND_PHOTO).setValue(downloadUrl.toString())

        Resource.Success(downloadUrl.toString())
    }

    override suspend fun deactivateAccount(): Resource<Unit> = safeResource {
        userReference.child(currentUser.uid).child(FirebasePaths.ACTIVE_ACCOUNT).setValue(false).await()
        Resource.Success(Unit)
    }

    override suspend fun addActivity(runId: String): Resource<Unit> = safeResource {
        val runsList = userReference.child(currentUser.uid).child(FirebasePaths.USER_DATA)
            .child(FirebasePaths.RUNS_LIST).get().await().getValue<List<String>>() ?: emptyList()
        val runsUpdatedList = runsList.toMutableList()
        runsUpdatedList.add(runId)
        userReference.child(currentUser.uid).child(FirebasePaths.USER_DATA).child(FirebasePaths.RUNS_LIST).setValue(runsUpdatedList).await()
        Resource.Success(Unit)
    }
}
