package com.example.fitness_tracking_app.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

object RepositoryUtils {

    inline fun <T> safeResource(firebaseAction: () -> Resource<T>): Resource<T> {
        return try {
            firebaseAction()
        } catch (e: Exception) {
            Resource.Error(e.message ?: GlobalStrings.ERROR_UNKNOWN)
        }
    }

    suspend inline fun <T : Any> safeFirebaseCall(
        crossinline firebaseAction: suspend () -> T,
        crossinline onSuccessAction: suspend (T) -> Unit
    ): Resource<T> {
        return try {
            val result = withContext(Dispatchers.IO) { firebaseAction() }
            withContext(Dispatchers.IO) { onSuccessAction(result) }
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: GlobalStrings.ERROR_UNKNOWN)
        }
    }

    inline fun <reified T> observeFirebaseReference(
        reference: DatabaseReference,
        crossinline transform: (DataSnapshot) -> T
    ): Flow<T> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(transform(snapshot))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }
}