package com.example.fitness_tracking_app.features.start_tracking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.data.FriendshipData
import com.example.fitness_tracking_app.repo.FriendsRepository
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartNewRecordViewModel @Inject constructor(
    private val friendsRepository: FriendsRepository
) : ViewModel() {

    private val _friendsList = MutableLiveData<Resource<List<FriendshipData>>>()
    val friendsList: LiveData<Resource<List<FriendshipData>>> = _friendsList

    init { observeFriendsId() }

    private fun observeFriendsId() {
        viewModelScope.launch(Dispatchers.IO) {
            _friendsList.postValue(Resource.Loading())
            friendsRepository.fetchFriendsIdFlow().catch { error ->
                _friendsList.postValue(Resource.Error(error.message))
            }.collect{ ids ->
                if (ids.isNullOrEmpty()) {
                    _friendsList.postValue(Resource.Success(emptyList()))
                } else {
                    fetchFriends(ids = ids)
                }
            }
        }
    }

    private suspend fun fetchFriends(ids: List<String>) {
        val friendsResult = friendsRepository.fetchFriends(ids)
        _friendsList.postValue(friendsResult)
    }
}