package com.example.fitness_tracking_app.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthManager {

    private var authStateListener : ((FirebaseUser?) -> Unit)? = null
    private val firebaseAuth = FirebaseAuth.getInstance()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            authStateListener?.invoke(auth.currentUser)
        }
    }
    fun addAuthStateObserver(observer: (FirebaseUser?) -> Unit) {
        authStateListener = observer
        observer(firebaseAuth.currentUser)
    }

    fun removeAuthStateObserver() {
        authStateListener = null
    }
}