package com.example.fitness_tracking_app.repo

import com.example.fitness_tracking_app.data.FriendshipStatus
import com.example.fitness_tracking_app.data.PersonalData
import com.example.fitness_tracking_app.data.UserData
import com.example.fitness_tracking_app.database.dao.UserDao
import com.example.fitness_tracking_app.common.FirebasePaths
import com.example.fitness_tracking_app.utils.Resource
import com.google.android.gms.tasks.Tasks
import org.junit.Assert.*
import org.junit.Test
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class UserRepositoryTest {

    private val uid = "123"

    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userDao: UserDao
    private lateinit var userRepository: UserRepository
    private lateinit var currentUser: FirebaseUser
    private lateinit var userReference: DatabaseReference
    private lateinit var userDataReference: DatabaseReference

    private lateinit var dataSnapshot: DataSnapshot

    @Before
    fun setUp() {
        firebaseDb = mock(FirebaseDatabase::class.java)
        firebaseAuth = mock(FirebaseAuth::class.java)
        userDao = mock(UserDao::class.java)

        currentUser = mock(FirebaseUser::class.java)
        `when`(firebaseAuth.currentUser).thenReturn(currentUser)
        `when`(currentUser.uid).thenReturn(uid)

        userReference = mock(DatabaseReference::class.java)
        `when`(firebaseDb.getReference(FirebasePaths.USER)).thenReturn(userReference)
        `when`(userReference.child(uid)).thenReturn(userReference)

        userDataReference = mock(DatabaseReference::class.java)
        `when`(userReference.child(FirebasePaths.USER_DATA)).thenReturn(userDataReference)

        dataSnapshot = mock(DataSnapshot::class.java)

        doNothing().`when`(userReference).keepSynced(true)

        userRepository = UserRepository(firebaseDb, firebaseAuth, userDao)
    }

    @Test
    fun `deactivateAccount should set activeAccount to false`() {
        runBlocking {
            val activeAccountRef = mock(DatabaseReference::class.java)

            `when`(userReference.child(uid).child(FirebasePaths.ACTIVE_ACCOUNT)).thenReturn(activeAccountRef)
            `when`(activeAccountRef.setValue(false)).thenReturn(Tasks.forResult(null))

            val result = userRepository.deactivateAccount()

            assertTrue(result is Resource.Success)
            verify(activeAccountRef).setValue(false)
        }
    }

    @Test
    fun `deactivateAccount return Error`() {
        runBlocking {
            val activeAccountRef = mock(DatabaseReference::class.java)
            `when`(userReference.child(uid).child(FirebasePaths.ACTIVE_ACCOUNT)).thenReturn(activeAccountRef)
            doThrow(RuntimeException("Test exception")).`when`(activeAccountRef).setValue(false)

            val result = userRepository.deactivateAccount()

            assertTrue(result is Resource.Error)
            assertEquals("Test exception", (result as Resource.Error).message)
        }
    }

    @Test
    fun `ifUserExists should return user status from snapshot`() {
        runBlocking {
            val activeAccountRef = mock(DatabaseReference::class.java)

            `when`(userReference.child(uid).child(FirebasePaths.ACTIVE_ACCOUNT)).thenReturn(activeAccountRef)

            doReturn(true).`when`(dataSnapshot).getValue(any<GenericTypeIndicator<Boolean>>())

            doAnswer { invocation ->
                val listener = invocation.getArgument<ValueEventListener>(0)
                listener.onDataChange(dataSnapshot)
                null
            }.`when`(activeAccountRef).addListenerForSingleValueEvent(any())

            val result = userRepository.ifUserExists()

            assertTrue(result is Resource.Success)
            assertEquals(true, (result as Resource.Success).data)
        }
    }

    //To check
    @Test
    fun `getUserFromDatabase should return user data`() = runBlocking {
        val personalData = PersonalData(
            gender = "woman",
            phoneNumber = 2300223,
            birthday = 124,
            height = 123,
            weightList = null
        )
        val expectedUserData = UserData(
            uid = uid,
            username = "Ewa_dev",
            email = "ewabmikulska@gmail.com",
            name = "Ewa",
            profilePhoto = "profile.jpg",
            backgroundPhoto = "background.jpg",
            runsList = listOf("run1", "run2"),
            jointRunsMap = mapOf("run1" to listOf("user1", "user2")),
            friends = mapOf("user2" to FriendshipStatus.ACCEPTED),
            personalData = personalData,
            messagesMap = mapOf("user2" to "Hello")
        )

        `when`(userDataReference.get()).thenReturn(Tasks.forResult(dataSnapshot))
        `when`(dataSnapshot.getValue(UserData::class.java)).thenReturn(expectedUserData)

        val result = userRepository.getUserFromDatabase()

        assertTrue(result is Resource.Success)
        assertEquals(expectedUserData, (result as Resource.Success).data)
    }
}