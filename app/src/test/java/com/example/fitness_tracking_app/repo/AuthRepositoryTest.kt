package com.example.fitness_tracking_app.repo

import com.example.fitness_tracking_app.common.FirebasePaths
import com.example.fitness_tracking_app.utils.Resource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthRepositoryTest {

    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authRepository: AuthRepository
    private lateinit var currentUser: FirebaseUser
    private lateinit var authResult: AuthResult
    private lateinit var activeReference: DatabaseReference

    private val email = "ewa@gmail.com"
    private val password = "password"
    private val error = "Test error"
    private val exception = RuntimeException(error)
    private val id = "id"

    @Before
    fun setUp() {
        firebaseDb = mock(FirebaseDatabase::class.java)
        firebaseAuth = mock(FirebaseAuth::class.java)
        currentUser = mock(FirebaseUser::class.java)
        authResult = mock(AuthResult::class.java)
        activeReference = mock(DatabaseReference::class.java)

        `when`(firebaseAuth.currentUser).thenReturn(currentUser)
        `when`(currentUser.uid).thenReturn(id)
        `when`(firebaseDb.getReference(FirebasePaths.ACTIVE_STATUS)).thenReturn(activeReference)
        `when`(activeReference.child(id)).thenReturn(activeReference)
        `when`(currentUser.email).thenReturn(email)

        authRepository = AuthRepository(firebaseDb, firebaseAuth)
    }

    @Test
    fun `isUserLoggedIn returns true`() {
        assertTrue(authRepository.isUserLoggedIn())
    }

    @Test
    fun `isUserLoggedIn returns false`() {
        `when`(firebaseAuth.currentUser).thenReturn(null)
        assertFalse(authRepository.isUserLoggedIn())
    }

    @Test
    fun `authResetPasswordWithEmail returns Success`() = runBlocking {
        `when`(firebaseAuth.sendPasswordResetEmail(email)).thenReturn(Tasks.forResult(null))

        val result = authRepository.authResetPasswordWithEmail(email)

        assertTrue(result is Resource.Success)
    }

    @Test
    fun `authResetPasswordWithEmail returns Error`() = runBlocking {
        `when`(firebaseAuth.sendPasswordResetEmail(email)).thenReturn(Tasks.forException(exception))

        val result = authRepository.authResetPasswordWithEmail(email)

        assertTrue(result is Resource.Error)
        assertEquals(error, (result as Resource.Error).message)
    }

    @Test
    fun `registerUser returns Success`() = runBlocking {
        `when`(firebaseAuth.createUserWithEmailAndPassword(email, password)).thenReturn(Tasks.forResult(authResult))

        val result  = authRepository.registerUser(email, password)

        assertTrue(result is Resource.Success)
    }

    @Test
    fun `registerUser returns Error`() = runBlocking {
        `when`(firebaseAuth.createUserWithEmailAndPassword(email, password)).thenReturn(Tasks.forException(exception))

        val result  = authRepository.registerUser(email, password)

        assertTrue(result is Resource.Error)
        assertEquals(error, (result as Resource.Error).message)
    }

    @Test
    fun `login returns Success`() = runBlocking {
        `when`(firebaseAuth.signInWithEmailAndPassword(email, password)).thenReturn(Tasks.forResult(authResult))

        val result = authRepository.login(email, password)

        assertTrue(result is Resource.Success)
    }

    @Test
    fun `login returns Error`() = runBlocking {
        `when`(firebaseAuth.signInWithEmailAndPassword(email, password)).thenReturn(Tasks.forException(exception))

        val result  = authRepository.login(email, password)

        assertTrue(result is Resource.Error)
        assertEquals(error, (result as Resource.Error).message)
    }

    @Test
    fun `signOut returns Success`() {
        `when`(activeReference.setValue(false)).thenReturn(Tasks.forResult(null))

        authRepository.signOut()

        verify(activeReference).setValue(false)

        verify(firebaseAuth).signOut()
    }

    @Test
    fun `signOut returns exception`() {
        doThrow(exception).`when`(activeReference).setValue(false)

        authRepository.signOut()

        verify(firebaseAuth).signOut()
    }

    @Test
    fun `checkCredentials returns Success`() = runBlocking {
        `when`(currentUser.reauthenticate(any())).thenReturn(Tasks.forResult(null))

        val result = authRepository.checkCredentials(password)

        assertTrue(result is Resource.Success)
    }

    @Test
    fun `checkCredentials returns Error`() = runBlocking {
        `when`(currentUser.reauthenticate(any())).thenReturn(Tasks.forException(Exception(error)))

        val result = authRepository.checkCredentials(password)

        assertTrue(result is Resource.Error)
        result as Resource.Error
        assertEquals(error, result.message)
    }

    @Test
    fun `removeAccount returns Success`() = runBlocking {
        `when`(currentUser.delete()).thenReturn(Tasks.forResult(null))

        val result = authRepository.removeAccount()

        assertTrue(result is Resource.Success)
    }
}
