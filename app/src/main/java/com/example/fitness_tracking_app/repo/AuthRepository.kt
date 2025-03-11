package com.example.fitness_tracking_app.repo

import com.example.fitness_tracking_app.BuildConfig
import com.example.fitness_tracking_app.common.FirebasePaths
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.utils.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class AuthRepository(
    private val firebaseDb: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : IAuthRepository {

    private val currentUser : FirebaseUser
        get() = firebaseAuth.currentUser ?: throw Exception(GlobalStrings.ERROR_USER_NOT_AUTHENTICATED)

    private val activeReference: DatabaseReference
        get() = firebaseDb.getReference(FirebasePaths.ACTIVE_STATUS)

    override fun isUserLoggedIn(): Boolean { return firebaseAuth.currentUser != null }

    override suspend fun authResetPasswordWithEmail(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message)
        }
    }

    override suspend fun registerUser(email: String, password: String): Resource<AuthResult> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message)
        }
    }

    override suspend fun login(email: String, password: String): Resource<AuthResult> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message)
        }
    }

    override fun signOut() {
        try {
            activeReference.child(currentUser.uid).setValue(false)
        } catch (e: Exception) {
            Timber.e("sign out error: $e")
        } finally {
            firebaseAuth.signOut()
        }
    }

    override suspend fun checkCredentials(password: String): Resource<Unit> {
        val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
        return try {
            currentUser.reauthenticate(credential).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message)
        }
    }

    override suspend fun removeAccount(): Resource<Unit> {
        return try {
            currentUser.delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message)
        }
    }
}
