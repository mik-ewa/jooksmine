package com.example.fitness_tracking_app.repo

import com.example.fitness_tracking_app.BuildConfig
import com.example.fitness_tracking_app.data.ChatData
import com.example.fitness_tracking_app.data.ChatDetails
import com.example.fitness_tracking_app.data.ChatMetadata
import com.example.fitness_tracking_app.data.MessageData
import com.example.fitness_tracking_app.data.MessageDataSender
import com.example.fitness_tracking_app.data.MsgData
import com.example.fitness_tracking_app.autoMessage
import com.example.fitness_tracking_app.common.FirebasePaths
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.utils.RepositoryUtils.observeFirebaseReference
import com.example.fitness_tracking_app.utils.RepositoryUtils.safeResource
import com.example.fitness_tracking_app.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val firebaseDb: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : IChatRepository {

    private val chatReference: DatabaseReference
        get() = firebaseDb.getReference(FirebasePaths.CHAT)

    private val userReference: DatabaseReference
        get()= firebaseDb.getReference(FirebasePaths.USER)

    private val currentUser : FirebaseUser
        get() = firebaseAuth.currentUser ?: throw Exception(GlobalStrings.ERROR_USER_NOT_AUTHENTICATED)

    override fun observeChatDetailsFlow(chatId: String): Flow<ChatDetails> {
        val chatRef = chatReference.child(chatId)
        return observeFirebaseReference(chatRef) { snapshot ->
            val chatMetadata = snapshot.child(FirebasePaths.CHAT_METADATA).getValue<ChatMetadata>()
            val messagesReference = snapshot.child(FirebasePaths.MESSAGES)
            val friendId = chatMetadata?.participants?.keys?.firstOrNull { it != currentUser.uid }!!
            val lastMessageSnapshot = messagesReference.children.maxByOrNull { it.key?.toInt() ?: 0 }
            val messageData = lastMessageSnapshot?.getValue<MessageData>()

            ChatDetails(
                friendId = friendId,
                chatId = chatId,
                message = messageData?.message ?: GlobalStrings.PHOTO,
                date = messageData?.timeStamp ?: 0L,
                isYourLastMsg = (messageData?.senderId != friendId),
                isRead = (messageData?.timeStamp ?: 0L) < (chatMetadata.lastRead)
            )
        }
    }

    override fun observeMessagesMapFlow(): Flow<Collection<String>> {
        val messagesMapRef = userReference.child(currentUser.uid).child(FirebasePaths.USER_DATA).child(
            FirebasePaths.MESSAGES_MAP)
        return observeFirebaseReference(messagesMapRef) { snapshot ->
            val chatMap = snapshot.getValue<Map<String, String>>() ?: emptyMap()
            chatMap.values
        }
    }

     suspend fun fetchChatMetadata(chatId: String): ChatMetadata {
        val chatMetadataSnap = chatReference.child(chatId).child(FirebasePaths.CHAT_METADATA).get().await()
        return chatMetadataSnap.getValue<ChatMetadata>() ?: ChatMetadata()
    }

    suspend fun fetchLastMessage(chatId: String): MessageData? {
        val chatMessagesSnap =
            chatReference.child(chatId).child(FirebasePaths.MESSAGES).limitToLast(1).get().await()
        return chatMessagesSnap.children.firstOrNull()?.getValue<MessageData>()
    }

    override suspend fun readMessage(chatId: String): Resource<Unit> = safeResource {
        fetchLastMessage(chatId)?.let { lastMessage ->
            val lastMessageTime = lastMessage.timeStamp
            val lastMessageSender = lastMessage.senderId

            val chatMetadata = fetchChatMetadata(chatId)

            val lastMessageRead = chatMetadata.lastRead

            if (lastMessageRead < lastMessageTime && lastMessageSender != currentUser.uid) {
                chatReference.child(chatId).child(FirebasePaths.CHAT_METADATA)
                    .child(FirebasePaths.LAST_READ).setValue(System.currentTimeMillis())
            }
            Resource.Success(Unit)
        } ?: return Resource.Error(GlobalStrings.ERROR_CHAT_NOT_EXIST)
    }

    override suspend fun getChatFromMyDatabase(friendId: String) : Resource<String?> = safeResource {
        val messagesMapSnap = userReference.child(currentUser.uid).child(FirebasePaths.USER_DATA)
            .child(FirebasePaths.MESSAGES_MAP).get().await()
        val messagesMap = messagesMapSnap.getValue<Map<String, String>>()
        val chatId = messagesMap?.get(friendId)
        Resource.Success(chatId)
    }

    override suspend fun sendMessage(msgData: MsgData, message: String): Resource<Unit> =
        safeResource {
            val messageData = MessageData(
                message = message,
                senderId = currentUser.uid,
                timeStamp = System.currentTimeMillis()
            )
            getMessageChat(msgData, messageData)
        }

    private suspend fun getMessageChat(
        msgData: MsgData,
        messageData: MessageData,
    ): Resource<Unit> = safeResource {
        val chatSnapshot = chatReference.child(msgData.chatId).get().await()
        val chatData = chatSnapshot.getValue<ChatData>()
        if (chatData == null) {
            sendMessageToNewChat(msgData, messageData)
        } else {
            sendMessageToExistingChat(chatData, messageData)
        }
    }

    private suspend fun sendMessageToNewChat(
        msgData: MsgData,
        messageData: MessageData
    ): Resource<Unit> = safeResource {
        val deferred = CompletableDeferred<Resource<Unit>>()

        val newChatData = ChatData(
            chatMetadata = ChatMetadata(
                chatId = msgData.chatId,
                participants = mapOf(currentUser.uid to 1, msgData.friendId to 1)
            ),
            messages = listOf(messageData)
        )

        chatReference.child(msgData.chatId).setValue(newChatData).addOnSuccessListener {
            CoroutineScope(Dispatchers.IO).launch {
                val result = runMessagesTransaction(msgData)
                deferred.complete(result)
            }
        }.addOnFailureListener { error ->
            deferred.complete(Resource.Error(error.message))
        }
        deferred.await()
    }

    private suspend fun sendMessageToExistingChat(
        chatData: ChatData,
        messageData: MessageData
    ): Resource<Unit> = safeResource {
        val mutableMessagesList = chatData.messages?.toMutableList() ?: mutableListOf()
        mutableMessagesList.add(messageData)
        chatReference.child(chatData.chatMetadata?.chatId!!).child(FirebasePaths.MESSAGES)
            .setValue(mutableMessagesList).await()
        Resource.Success(Unit)
    }

    private suspend fun runMessagesTransaction(msgData: MsgData): Resource<Unit> = safeResource {
        val deferred = CompletableDeferred<Resource<Unit>>()

        userReference.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val myMessageMap = currentData.child(currentUser.uid).child(FirebasePaths.USER_DATA)
                        .child(FirebasePaths.MESSAGES_MAP).getValue<Map<String, String>>()
                val friendsMessageMap = currentData.child(msgData.friendId).child(FirebasePaths.USER_DATA)
                        .child(FirebasePaths.MESSAGES_MAP).getValue<Map<String, String>>()
                val myMessageMapMutable = myMessageMap?.toMutableMap() ?: mutableMapOf()
                val myFriendsMapMutable = friendsMessageMap?.toMutableMap() ?: mutableMapOf()

                myMessageMapMutable[msgData.friendId] = msgData.chatId
                myFriendsMapMutable[currentUser.uid] = msgData.chatId

                currentData.child(currentUser.uid).child(FirebasePaths.USER_DATA)
                    .child(FirebasePaths.MESSAGES_MAP).value = myMessageMapMutable
                currentData.child(msgData.friendId).child(FirebasePaths.USER_DATA)
                    .child(FirebasePaths.MESSAGES_MAP).value = myFriendsMapMutable

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed) {
                    deferred.complete(Resource.Success(Unit))
                } else {
                    deferred.complete(Resource.Error(error?.message))
                }
            }
        })
        deferred.await()
    }

    override fun showChatWithMessages(
        chatID: String,
        onDataChanged: (Resource<Pair<ChatMetadata?, List<MessageDataSender>?>>) -> Unit
    ): ValueEventListener {
        val chatReference = chatReference.child(chatID)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatSnap = snapshot.getValue<ChatData>()
                val chatMetadata = chatSnap?.chatMetadata
                val messages = chatSnap?.messages?.map { message ->
                    message.let {
                        MessageDataSender(
                            message = it.message,
                            sender = it.senderId == currentUser.uid,
                            imageURL = it.imageURL,
                            timeStamp = it.timeStamp
                        )
                    }
                }

                onDataChanged(Resource.Success(Pair(chatMetadata, messages)))
            }

            override fun onCancelled(error: DatabaseError) {
                onDataChanged(Resource.Error(error.message))
            }
        }

        chatReference.addValueEventListener(listener)
        return listener
    }

    override fun removeMessagesListener(chatId: String, listener: ValueEventListener) {
        val chatReference = chatReference.child(chatId)
        chatReference.removeEventListener(listener)
    }

    suspend fun sendAutoMessage(msgData: MsgData): Resource<Unit> =
    safeResource {
        val messageData = MessageData(
            message = autoMessage,
            senderId = BuildConfig.MAIN_FIREBASE_USER,
            timeStamp = System.currentTimeMillis()
        )
        getMessageChat(msgData, messageData)
    }
}
