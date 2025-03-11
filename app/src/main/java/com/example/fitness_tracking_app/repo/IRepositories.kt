package com.example.fitness_tracking_app.repo

import android.net.Uri
import com.example.fitness_tracking_app.data.ChatDetails
import com.example.fitness_tracking_app.data.ChatMetadata
import com.example.fitness_tracking_app.data.CompletedRunData
import com.example.fitness_tracking_app.data.FirebaseRunData
import com.example.fitness_tracking_app.data.FriendBaseProfileData
import com.example.fitness_tracking_app.data.FriendshipData
import com.example.fitness_tracking_app.data.FriendshipStatus
import com.example.fitness_tracking_app.data.JointActivityData
import com.example.fitness_tracking_app.data.LanguagesResponse
import com.example.fitness_tracking_app.data.MessageDataSender
import com.example.fitness_tracking_app.data.MsgData
import com.example.fitness_tracking_app.data.PersonalData
import com.example.fitness_tracking_app.data.UserData
import com.example.fitness_tracking_app.data.UserLoginData
import com.example.fitness_tracking_app.data.Weights
import com.example.fitness_tracking_app.database.Motivation
import com.example.fitness_tracking_app.database.entity.User
import com.example.fitness_tracking_app.models.WeatherResponse
import com.example.fitness_tracking_app.utils.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface IAuthRepository {
    fun isUserLoggedIn(): Boolean
    suspend fun authResetPasswordWithEmail(email: String): Resource<Unit>
    suspend fun registerUser(email: String, password: String): Resource<AuthResult>
    suspend fun login(email: String, password: String): Resource<AuthResult>
    fun signOut()
    suspend fun removeAccount(): Resource<Unit>
    suspend fun checkCredentials(password: String): Resource<Unit>
}

interface IFriendsRepository {
    fun getCurrentStatusFlow(friendId: String): Flow<FriendshipStatus?>
    suspend fun fetchFriend(friendId: String): Resource<FriendBaseProfileData>
    suspend fun findWithUsername(username: String): Resource<String?>
    suspend fun changeFriendStatus(friendId: String, status: FriendshipStatus?): Resource<Unit>
    suspend fun fetchFriends(friends: List<String>): Resource<List<FriendshipData>>
    fun fetchFriendsIdFlow(): Flow<List<String>?>
}

interface IUserRepository {
    suspend fun getUserFromDao(): User?
    suspend fun ifUserExists(): Resource<Boolean?>
    fun getUserFlowFromDao(): Flow<User?>
    suspend fun setStatus(isActive: Boolean)
    suspend fun getUserFromDatabase(): Resource<UserData?>
    suspend fun fetchJointActivity(friendId: String): Resource<List<JointActivityData>?>
    suspend fun checkUsername(username: String): Resource<Boolean>
    suspend fun saveUser(userLoginData: UserLoginData): Resource<String>
    suspend fun saveUserToDao(userData: UserData)
    suspend fun saveProfilePhoto(picture: Uri): Resource<String>
    suspend fun saveBackgroundPhoto(picture: Uri): Resource<String>
    suspend fun deactivateAccount(): Resource<Unit>
    suspend fun addActivity(runId: String): Resource<Unit>
}

interface IChatRepository{
    fun observeChatDetailsFlow(chatId: String): Flow<ChatDetails>
    fun observeMessagesMapFlow(): Flow<Collection<String>>
    suspend fun readMessage(chatId: String): Resource<Unit>
    suspend fun getChatFromMyDatabase(friendId: String): Resource<String?>
    suspend fun sendMessage(msgData: MsgData, message: String): Resource<Unit>?
    fun showChatWithMessages(chatID: String, onDataChanged: (Resource<Pair<ChatMetadata?, List<MessageDataSender>?>>) -> Unit): ValueEventListener
    fun removeMessagesListener(chatId: String, listener: ValueEventListener)
}

interface IPersonalInformationRepository {
    suspend fun getUser() : User?
    fun getUserFlow():  Flow<User?>
    suspend fun saveMotivation(motivation: Motivation)
    suspend fun getUserPersonalDataFromDatabase(): Resource<PersonalData?>
    suspend fun updatePersonalData(personalData: PersonalData): Resource<Unit>
    suspend fun deleteWeight(weight: Weights)
    suspend fun addWeight(weight: Weights)
    fun getLanguages() : LanguagesResponse
}

interface IRunningRepository {
    suspend fun startRunning(startDate: Long): Resource<String>
    suspend fun getUserRuns(runList: List<String>?): List<FirebaseRunData>?
    suspend fun finishRunning(completedRunData : CompletedRunData): Resource<Unit>
    suspend fun removeRunningData(runId: String)
}

interface IWeatherRepository {
    suspend fun getCurrentWeather(long: Double, lat: Double, time: Long): Response<WeatherResponse>
}