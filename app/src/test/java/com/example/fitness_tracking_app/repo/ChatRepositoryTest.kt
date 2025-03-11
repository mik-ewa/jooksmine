package com.example.fitness_tracking_app.repo

import com.example.fitness_tracking_app.data.ChatMetadata
import com.example.fitness_tracking_app.common.FirebasePaths
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class ChatRepositoryTest {

    private val chatId = "chat123"
    private val uid = "123"

    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var chatReference: DatabaseReference
    private lateinit var userReference: DatabaseReference
    private lateinit var chatMetadataReference: DatabaseReference
    private lateinit var messagesReference: DatabaseReference
    private lateinit var lastReadReference: DatabaseReference

    private lateinit var snapshot: DataSnapshot
    private lateinit var messagesSnapshot: DataSnapshot
    private lateinit var messageSnapshot: DataSnapshot

    private lateinit var chatRepository: ChatRepository

    @Before
    fun setUp() {
        firebaseDb = mock(FirebaseDatabase::class.java)
        firebaseAuth = mock(FirebaseAuth::class.java)

        chatReference = mock(DatabaseReference::class.java)
        `when`(firebaseDb.getReference(FirebasePaths.CHAT)).thenReturn(chatReference)
        `when`(chatReference.child(chatId)).thenReturn(chatReference)

        userReference = mock(DatabaseReference::class.java)
        `when`(firebaseDb.getReference(FirebasePaths.USER)).thenReturn(userReference)
        `when`(userReference.child(uid)).thenReturn(userReference)

        currentUser = mock(FirebaseUser::class.java)
        `when`(firebaseAuth.currentUser).thenReturn(currentUser)
        `when`(currentUser.uid).thenReturn(uid)

        chatMetadataReference = mock(DatabaseReference::class.java)
        `when`(chatReference.child(chatId).child(FirebasePaths.CHAT_METADATA)).thenReturn(chatMetadataReference)

        messagesReference = mock(DatabaseReference::class.java)
        `when`(chatReference.child(chatId).child(FirebasePaths.MESSAGES)).thenReturn(messagesReference)
        `when`(messagesReference.limitToLast(1)).thenReturn(messagesReference)

        lastReadReference = mock(DatabaseReference::class.java)
        `when`(chatMetadataReference.child(FirebasePaths.LAST_READ)).thenReturn(lastReadReference)

        snapshot = mock(DataSnapshot::class.java)
        messagesSnapshot = mock(DataSnapshot::class.java)
        messageSnapshot = mock(DataSnapshot::class.java)

        chatRepository = ChatRepository(firebaseDb, firebaseAuth)
    }

    @Test
    fun `fetchChatMetadata returns ChatMetadata from snapshot`() = runTest {
        val expectedMetadata = ChatMetadata(lastRead = 123, chatId = chatId, participants = mapOf("user1" to 1, "user2" to 1))

        `when`(chatMetadataReference.get()).thenReturn(Tasks.forResult(snapshot))
        `when`(snapshot.exists()).thenReturn(true)
        doReturn(expectedMetadata).`when`(snapshot).getValue<ChatMetadata>()

        val result = chatRepository.fetchChatMetadata(chatId)

        assertEquals(expectedMetadata, result)
    }

    @Test
    fun `fetchChatMetadata returns default ChatMetadata when snapshot is null`() = runTest {
        `when`(chatMetadataReference.get()).thenReturn(Tasks.forResult(snapshot))
        `when`(snapshot.getValue(ChatMetadata::class.java)).thenReturn(null)

        val result = chatRepository.fetchChatMetadata(chatId)

        assertEquals(ChatMetadata(), result)
    }
}