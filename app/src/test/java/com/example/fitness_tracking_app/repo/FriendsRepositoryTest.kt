package com.example.fitness_tracking_app.repo

import com.example.fitness_tracking_app.data.FriendBaseProfileData
import com.example.fitness_tracking_app.data.FriendshipStatus
import com.example.fitness_tracking_app.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import org.mockito.Mockito.mock
import com.example.fitness_tracking_app.common.FirebasePaths
import com.example.fitness_tracking_app.utils.Resource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class FriendsRepositoryTest {

    private val uid = "123"
    private val friendId = "friend1"
    private val error = "Test error"

    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var userReference: DatabaseReference
    private lateinit var activeReference: DatabaseReference
    private lateinit var userDataReference: DatabaseReference
    private lateinit var friendsReference: DatabaseReference
    private lateinit var friendReference: DatabaseReference
    private lateinit var activeAccountReference: DatabaseReference
    private lateinit var friendActiveRef: DatabaseReference

    private lateinit var snapshot: DataSnapshot

    private lateinit var friendsRepository: FriendsRepository

    @Before
    fun setUp() {
        firebaseDb = mock(FirebaseDatabase::class.java)
        firebaseAuth = mock(FirebaseAuth::class.java)
        currentUser = mock(FirebaseUser::class.java)
        snapshot = mock(DataSnapshot::class.java)

        `when`(firebaseAuth.currentUser).thenReturn(currentUser)
        `when`(currentUser.uid).thenReturn(uid)

        userReference = mock(DatabaseReference::class.java)
        `when`(firebaseDb.getReference(FirebasePaths.USER)).thenReturn(userReference)
        `when`(userReference.child(uid)).thenReturn(userReference)

        activeReference = mock(DatabaseReference::class.java)
        `when`(firebaseDb.getReference(FirebasePaths.ACTIVE_STATUS)).thenReturn(activeReference)

        userDataReference = mock(DatabaseReference::class.java)
        `when`(userReference.child(FirebasePaths.USER_DATA)).thenReturn(userDataReference)

        friendsReference = mock(DatabaseReference::class.java)
        `when`(userDataReference.child(FirebasePaths.FRIENDS)).thenReturn(friendsReference)

        friendReference = mock(DatabaseReference::class.java)
        `when`(friendsReference.child(friendId)).thenReturn(friendReference)

        activeAccountReference = mock(DatabaseReference::class.java)
        `when`(userReference.child(FirebasePaths.ACTIVE_ACCOUNT)).thenReturn(activeAccountReference)

        friendActiveRef = mock(DatabaseReference::class.java)
        `when`(activeReference.child(friendId)).thenReturn(friendActiveRef)

        friendsRepository = FriendsRepository(firebaseDb, firebaseAuth)
    }

    @Test
    fun `getCurrentStatusFlow emits FriendshipStatus`() = runBlocking {
        val expectedStatus = FriendshipStatus.ACCEPTED

        doReturn(expectedStatus).`when`(snapshot).getValue(org.mockito.kotlin.any<GenericTypeIndicator<FriendshipStatus>>())

        doAnswer { invocation ->
            val listener = invocation.getArgument<ValueEventListener>(0)
            listener.onDataChange(snapshot)
            null
        }.`when`(friendsReference).addValueEventListener(any())

        val result = friendsRepository.getCurrentStatusFlow(friendId).first()

        assertEquals(expectedStatus, result)
    }

    @Test
    fun `getCurrentStatusFlow emits null`() = runBlocking {
        `when`(snapshot.getValue(FriendshipStatus::class.java)).thenReturn(null)

        doAnswer { invocation ->
            val listener = invocation.getArgument<ValueEventListener>(0)
            listener.onDataChange(snapshot)
            null
        }.`when`(friendsReference).addValueEventListener(any())

        val result = friendsRepository.getCurrentStatusFlow(friendId).first()

        assertNull(result)
    }

    @Test
    fun `checkUserAccountStatus returns false`() = runTest {
        `when`(activeAccountReference.get()).thenReturn(Tasks.forResult(snapshot))
        `when`(snapshot.getValue(Boolean::class.java)).thenReturn(false)

        val result = friendsRepository.checkUserAccountStatus(friendId)

        assertFalse(result)
    }

    @Test
    fun `checkUserAccountStatus returns false when snapshot is null`() = runTest {
        `when`(activeAccountReference.get()).thenReturn(Tasks.forResult(snapshot))
        `when`(snapshot.getValue(Boolean::class.java)).thenReturn(null)

        val result = friendsRepository.checkUserAccountStatus(friendId)
        assertFalse(result)
    }

    @Test
    fun `checkUserAccountStatus returns false when Exception`() = runTest {
        `when`(activeAccountReference.get()).thenReturn(Tasks.forException(Exception(error)))

        val result = friendsRepository.checkUserAccountStatus(friendId)
        assertFalse(result)
    }

    @Test
    fun `getFriendActiveStatus returns true`() = runTest {
        `when`(friendActiveRef.get()).thenReturn(Tasks.forResult(snapshot))
        `when`(snapshot.exists()).thenReturn(true)
        `when`(snapshot.getValue(Boolean::class.java)).thenReturn(true)

        val result = friendsRepository.getFriendActiveStatus(friendId)
        assertTrue(result)
    }

    @Test
    fun `getFriendActiveStatus returns false when snapshot does not exist`() = runTest {
        `when`(friendActiveRef.get()).thenReturn(Tasks.forResult(snapshot))
        `when`(snapshot.exists()).thenReturn(false)

        val result = friendsRepository.getFriendActiveStatus(friendId)
        assertFalse(result)
    }

    @Test
    fun `getFriendActiveStatus returns false Exception`() = runTest {
        `when`(friendActiveRef.get()).thenReturn(Tasks.forException(Exception(error)))

        val result = friendsRepository.getFriendActiveStatus(friendId)
        assertFalse(result)
    }

    @Test
    fun `fetchFriend returns FriendBaseProfileData`() = runTest {
        val userData = UserData(
            uid = friendId,
            username = "Ewa",
            email = "ewa@ewa.com",
            name = "Ewa",
            profilePhoto = "elo.jpg",
            backgroundPhoto = "elo.jpg",
            runsList = listOf("1", "2"),
            jointRunsMap = null,
            friends = null,
            personalData = null,
            messagesMap = null
        )

        `when`(userReference.child(friendId).child(FirebasePaths.USER_DATA).get()).thenReturn(Tasks.forResult(snapshot))
        `when`(snapshot.getValue(UserData::class.java)).thenReturn(userData)

        val result = friendsRepository.fetchFriend(friendId)

        val expectedFriend = FriendBaseProfileData(
            friendId = friendId,
            friendName = userData.name,
            friendUsername = userData.username,
            friendImage = userData.profilePhoto,
            friendBackgroundImage = userData.backgroundPhoto,
            friendActivityData = userData.runsList
        )

        assertEquals(expectedFriend, (result as Resource.Success).data)
    }
}