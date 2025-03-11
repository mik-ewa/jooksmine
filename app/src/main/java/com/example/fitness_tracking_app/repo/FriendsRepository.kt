package com.example.fitness_tracking_app.repo

import com.example.fitness_tracking_app.BuildConfig
import com.example.fitness_tracking_app.data.FriendBaseProfileData
import com.example.fitness_tracking_app.data.FriendData
import com.example.fitness_tracking_app.data.FriendshipData
import com.example.fitness_tracking_app.data.FriendshipStatus
import com.example.fitness_tracking_app.data.UserData
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
import com.google.firebase.database.getValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class FriendsRepository(
    private val firebaseDb: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : IFriendsRepository {

    private val currentUser : FirebaseUser
        get() = firebaseAuth.currentUser ?: throw Exception(GlobalStrings.ERROR_USER_NOT_AUTHENTICATED)

    private val userReference: DatabaseReference
        get()= firebaseDb.getReference(FirebasePaths.USER)

    private val activeReference: DatabaseReference
        get() = firebaseDb.getReference(FirebasePaths.ACTIVE_STATUS)

    private val usernameReference: DatabaseReference
        get() = firebaseDb.getReference(FirebasePaths.USERNAME)

    override fun getCurrentStatusFlow(friendId: String): Flow<FriendshipStatus?> {
        val friendRef = userReference.child(currentUser.uid).child(FirebasePaths.USER_DATA).child(
            FirebasePaths.FRIENDS).child(friendId)
        return observeFirebaseReference(friendRef) { snapshot ->
            snapshot.getValue<FriendshipStatus>()
        }
    }

    suspend fun checkUserAccountStatus(userId: String): Boolean {
        return try {
            val userActiveAccountSnap = userReference.child(userId).child(FirebasePaths.ACTIVE_ACCOUNT).get().await()
            val result = userActiveAccountSnap.getValue<Boolean>()
            result ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchFriendChatData(friendId: String): FriendData {
        return try {
            when (checkUserAccountStatus(friendId)) {
                true -> {
                    val friendNameSnap = userReference.child(friendId).child(FirebasePaths.USER_DATA)
                            .child(FirebasePaths.USERNAME).get().await()
                    val friendPhotoSnap = userReference.child(friendId).child(FirebasePaths.USER_DATA)
                            .child(FirebasePaths.PROFILE_PHOTO).get().await()
                    val friendStatus = getFriendActiveStatus(friendId)
                    val username = friendNameSnap.getValue<String>()!!
                    val photo = friendPhotoSnap.getValue<String>()!!
                    FriendData(username, photo, friendStatus)
                }

                false -> {
                    FriendData(
                        username = GlobalStrings.JOOKSMINE_USER,
                        photo = GlobalStrings.PHOTO_USER_DELETED,
                        isFriendActive = false
                    )
                }
            }
        } catch (e: Exception) {
            FriendData(
                username = GlobalStrings.JOOKSMINE_USER,
                photo = GlobalStrings.PHOTO_USER_DELETED,
                isFriendActive = true
            )
        }
    }

    suspend fun getFriendActiveStatus(friendId: String): Boolean {
        return try {
            val snap = activeReference.child(friendId).get().await()
            if (snap.exists()) snap.getValue<Boolean>()!! else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun fetchFriend(friendId: String): Resource<FriendBaseProfileData> = safeResource {
        val friendDataSnapshot = userReference.child(friendId).child(FirebasePaths.USER_DATA).get().await()

        val friendData = friendDataSnapshot.getValue<UserData>()

        val friend = FriendBaseProfileData(
            friendId = friendId,
            friendName = friendData?.name,
            friendUsername = friendData?.username,
            friendImage = friendData?.profilePhoto,
            friendBackgroundImage = friendData?.backgroundPhoto,
            friendActivityData = friendData?.runsList,
        )
        Resource.Success(friend)
    }

    override suspend fun findWithUsername(username: String): Resource<String?> = safeResource {
        val datasnap = usernameReference.child(username.lowercase()).get().await()
        val userId = datasnap.getValue<String>()
        Resource.Success(userId)
    }

    override suspend fun changeFriendStatus(
        friendId: String,
        status: FriendshipStatus?
    ): Resource<Unit> = safeResource {
        val deferred = CompletableDeferred<Resource<Unit>>()
        val myId = currentUser.uid

        userReference.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val myData = currentData.child(myId).child(FirebasePaths.USER_DATA).getValue<UserData>()
                val friendData = currentData.child(friendId).child(FirebasePaths.USER_DATA).getValue<UserData>() ?: return Transaction.abort()
                val myFriends = myData?.friends?.toMutableMap() ?: mutableMapOf()
                val friendFriends = friendData.friends?.toMutableMap() ?: mutableMapOf()
                when (status) {
                    null -> {
                        myFriends.remove(friendId)
                        friendFriends.remove(myId)
                    }

                    FriendshipStatus.PENDING -> {
                        myFriends[friendId] = status
                        friendFriends[myId] = FriendshipStatus.REQUEST
                    }

                    else -> {
                        myFriends[friendId] = status
                        friendFriends[myId] = status
                    }
                }
                currentData.child(myId).child(FirebasePaths.USER_DATA).child(FirebasePaths.FRIENDS).value = myFriends
                currentData.child(friendId).child(FirebasePaths.USER_DATA).child(FirebasePaths.FRIENDS).value = friendFriends

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?
            ) {
                if (committed) { deferred.complete(Resource.Success(Unit)) } else { deferred.complete(Resource.Error(error?.message)) }
            }
        })
        deferred.await()
    }

    override suspend fun fetchFriends(friends: List<String>): Resource<List<FriendshipData>> =
        safeResource {
            val friendsList: MutableList<FriendshipData> = mutableListOf()
            for (friendId in friends) {
                val friendSnap = userReference.child(friendId).child(FirebasePaths.USER_DATA).get().await()
                if (friendSnap.exists()) {
                    val friendData = friendSnap.getValue<UserData>()
                    val friendName = friendData?.name!!
                    val friendUsername = friendData.username
                    val friendImage = friendData.profilePhoto

                    val friend = FriendshipData(
                        friendNickname = friendUsername!!,
                        friendID = friendId,
                        friendName = friendName,
                        friendImage = friendImage!!,
                        friendStatus = FriendshipStatus.ACCEPTED
                    )
                    friendsList.add(friend)
                }
            }
            Resource.Success(friendsList)
        }

    override fun fetchFriendsIdFlow(): Flow<List<String>?> {
        val friendsRef = userReference.child(currentUser.uid).child(FirebasePaths.USER_DATA).child(
            FirebasePaths.FRIENDS)
        return observeFirebaseReference(friendsRef) { snapshot ->
            val friendMap = snapshot.getValue<Map<String, FriendshipStatus>>() ?: emptyMap()
            friendMap.filter { it.value == FriendshipStatus.ACCEPTED }.keys.toList()
        }
    }
}
