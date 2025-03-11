package com.example.fitness_tracking_app.features.complete_sign_up

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.BuildConfig
import com.example.fitness_tracking_app.data.FriendshipStatus
import com.example.fitness_tracking_app.data.UserLoginData
import com.example.fitness_tracking_app.repo.FriendsRepository
import com.example.fitness_tracking_app.repo.UserRepository
import com.example.fitness_tracking_app.common.FirebasePaths
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompleteSignUpViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val friendsRepository: FriendsRepository
) : ViewModel() {

    private val _saveUserStatus = MutableLiveData<Resource<String>>()
    val saveUserStatus: LiveData<Resource<String>> = _saveUserStatus

    private val _usernameResource = MutableLiveData<Resource<Boolean>>()
    val usernameResource: LiveData<Resource<Boolean>> = _usernameResource

    private val _friendsResult = MutableLiveData<Resource<Unit>>()
    val friendsStatus : LiveData<Resource<Unit>> get() = _friendsResult

    fun checkUsername(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _usernameResource.postValue(userRepository.checkUsername(username.lowercase()))
        }
    }

    fun saveUserDataToFirebase(user: UserLoginData) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = userRepository.saveUser(user)
            if (result is Resource.Success && BuildConfig.AUTO_FRIEND_MODE) {
                addDefaultFriend()
            } else {
                _saveUserStatus.postValue(result)
            }
        }
    }

    fun addDefaultFriend() {
        viewModelScope.launch(Dispatchers.IO) {
            val friendshipResult = friendsRepository.changeFriendStatus(
                BuildConfig.MAIN_FIREBASE_USER,
                FriendshipStatus.ACCEPTED
            )
            _friendsResult.postValue(friendshipResult)
        }
    }
}