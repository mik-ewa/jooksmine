package com.example.fitness_tracking_app.features.friend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.data.ActivitiesPagerData
import com.example.fitness_tracking_app.data.FirebaseRunData
import com.example.fitness_tracking_app.data.FriendBaseProfileData
import com.example.fitness_tracking_app.data.FriendshipStatus
import com.example.fitness_tracking_app.data.JointActivityData
import com.example.fitness_tracking_app.repo.FriendsRepository
import com.example.fitness_tracking_app.repo.RunningRepository
import com.example.fitness_tracking_app.repo.UserRepository
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val friendsRepository: FriendsRepository,
    private val userRepository: UserRepository,
    private val runningRepository: RunningRepository,
) : ViewModel() {

    private lateinit var friendId: String

    private val _friendProfileBaseData = MutableLiveData<Resource<FriendBaseProfileData>>()
    val friendProfileBaseData: LiveData<Resource<FriendBaseProfileData>> get() = _friendProfileBaseData

    private var cachedBasedData: BaseData? = null

    private val _listJointActivityData = MutableLiveData<Resource<List<JointActivityData>?>>()
    private val _listRunData = MutableLiveData<Resource<List<FirebaseRunData>?>>()

    private val _combinedData = MediatorLiveData<Resource<ActivitiesPagerData?>>().apply {

        addSource(_listJointActivityData) { listJointActivityData ->
            val currentData = ActivitiesPagerData(
                myPhoto = cachedBasedData?.myPhoto,
                myName = cachedBasedData?.myName,
                friendsPhoto = cachedBasedData?.friendsPhoto,
                friendsName = cachedBasedData?.friendsName,
                jointActivityList = listJointActivityData.data ?: emptyList(),
                activityList = (_listRunData.value as? Resource.Success)?.data ?: emptyList()
            )
            if (cachedBasedData != null && listJointActivityData is Resource.Success) {
                value = Resource.Success(currentData)
            } else if (listJointActivityData is Resource.Loading) {
                value = Resource.Loading(currentData)
            }
        }
        addSource(_listRunData) { listRunData ->
            val currentData = ActivitiesPagerData(
                myPhoto = cachedBasedData?.myPhoto,
                myName = cachedBasedData?.myName,
                friendsPhoto = cachedBasedData?.friendsPhoto,
                friendsName = cachedBasedData?.friendsName,
                jointActivityList = (_listJointActivityData.value as? Resource.Success)?.data ?: emptyList(),
                activityList = listRunData.data ?: emptyList()
            )
            if (cachedBasedData != null && listRunData is Resource.Success) {
                value = Resource.Success(currentData)
            } else if (listRunData is Resource.Loading) {
                value = Resource.Loading(currentData)
            }
        }
    }
    val combinedData: LiveData<Resource<ActivitiesPagerData?>> = _combinedData

    private val _statusResource = MutableLiveData<Resource<Unit>>()
    val statusResource: LiveData<Resource<Unit>> = _statusResource

    private val _friendshipStatus = MutableLiveData<FriendshipStatus?>()
    val friendshipStatus: LiveData<FriendshipStatus?> = _friendshipStatus

    fun setFriendId(friendId: String) {
        this.friendId = friendId
        showFriendProfile(friendId)
    }

    private fun showFriendProfile(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            showFriend(id)
            showStatus(id)
        }
    }

    private suspend fun showStatus(friendId: String) {
        friendsRepository.getCurrentStatusFlow(friendId)
            .catch { _friendshipStatus.postValue(FriendshipStatus.REQUEST) }
            .collect { _friendshipStatus.postValue(it) }
    }

    fun changeFriendStatus(status: FriendshipStatus?) {
        val newStatus = when (status) {
            FriendshipStatus.REQUEST, null -> FriendshipStatus.ACCEPTED
            else -> null
        }
        viewModelScope.launch(Dispatchers.IO) {
            _statusResource.postValue(friendsRepository.changeFriendStatus(friendId, newStatus))
        }
    }

    private suspend fun showFriend(friendId: String) {
        val result = friendsRepository.fetchFriend(friendId)
        _friendProfileBaseData.postValue(result)
        if (result is Resource.Success) {
            val friendData = result.data
            val myData = getMyData()

            cachedBasedData = BaseData(
                myName = myData?.name,
                myPhoto = myData?.profilePhoto,
                friendsPhoto = friendData?.friendImage,
                friendsName = friendData?.friendName
            )
            val activitiesPagerData = ActivitiesPagerData(
                myPhoto = cachedBasedData?.myPhoto,
                myName = cachedBasedData?.myName,
                friendsPhoto = cachedBasedData?.friendsPhoto,
                friendsName = cachedBasedData?.friendsName,
                jointActivityList = emptyList(),
                activityList = emptyList()
            )
            _combinedData.postValue(Resource.Success(activitiesPagerData))
            if (!friendData?.friendActivityData.isNullOrEmpty()) {
                getFriendRuns(friendData?.friendActivityData!!)
            }
            getJointRuns(friendId)
        }
    }

    private suspend fun getMyData() = userRepository.getUserFromDao()

    private fun getFriendRuns(friendRuns: List<String>) {
        _listRunData.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            _listRunData.postValue(Resource.Success(runningRepository.getUserRuns(friendRuns)))
        }
    }

    private fun getJointRuns(friendId: String) {
        _listRunData.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            _listJointActivityData.postValue(userRepository.fetchJointActivity(friendId))
        }
    }
}

data class BaseData(
    val myName: String?,
    val myPhoto: String?,
    val friendsName: String?,
    val friendsPhoto: String?
)