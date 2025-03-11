package com.example.fitness_tracking_app.features.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.repo.FriendsRepository
import com.example.fitness_tracking_app.repo.PersonalInformationRepository
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindNewFriendViewModel @Inject constructor(
    private val friendsRepository: FriendsRepository,
    private val personalInformationRepository: PersonalInformationRepository
) : ViewModel() {

    private val _friendId = MutableLiveData<Resource<String?>>()
    val friendId: LiveData<Resource<String?>> = _friendId

    private val _isMyNickname = MutableLiveData<Boolean>()
    val isMyNickname: LiveData<Boolean> get() = _isMyNickname

    fun isYourNickname(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val myUsername = personalInformationRepository.getUser()?.username
            if (username.lowercase() == myUsername?.lowercase()) { _isMyNickname.postValue(true)
            } else { searchFriend(username) }
        }
    }

    private suspend fun searchFriend(username: String) {
        val result = friendsRepository.findWithUsername(username)
        _friendId.postValue(result)
    }
}
