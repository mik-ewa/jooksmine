package com.example.fitness_tracking_app.features

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.data.ChatInfo
import com.example.fitness_tracking_app.repo.ChatRepository
import com.example.fitness_tracking_app.repo.FriendsRepository
import com.example.fitness_tracking_app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val friendsRepository: FriendsRepository
) : ViewModel() {

    private val chatMap = mutableMapOf<String, ChatInfo>()

    private val _unreadChatsNumber = MutableLiveData<Int>()
    val unreadChatsNumber: LiveData<Int> get() = _unreadChatsNumber

    private val _chatMapWithInfo = MutableLiveData<Resource<Map<String, ChatInfo>>>()
    val chatMapWithInfo: LiveData<Resource<Map<String, ChatInfo>>> get() = _chatMapWithInfo

    init { observeChats() }

    private fun observeChats() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.observeMessagesMapFlow().catch { error ->
                _chatMapWithInfo.postValue(Resource.Error(error.message))
            }.collect { chatIds ->
                chatIds.forEach { id -> if (!chatMap.containsKey(id)) { observeSingleChat(id) } }
                if (chatIds.isEmpty()) {
                    _chatMapWithInfo.postValue(Resource.Success(emptyMap()))
                }
            }
        }
    }

    private fun observeSingleChat(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.observeChatDetailsFlow(id).catch { error ->
                _chatMapWithInfo.postValue(Resource.Error(error.message))
            }.collect { chatDetails ->
                var chatInfo = chatMap[id]
                if (chatInfo?.friendPhoto.isNullOrEmpty()) {
                    val friendInfo = friendsRepository.fetchFriendChatData(chatDetails.friendId)

                    chatInfo = ChatInfo(
                        date = chatDetails.date,
                        chatId = chatDetails.chatId,
                        message = chatDetails.message,
                        isRead = chatDetails.isRead,
                        isYourLastMsg = chatDetails.isYourLastMsg,
                        friendId = chatDetails.friendId,
                        friendNickname = friendInfo.username,
                        friendPhoto = friendInfo.photo,
                        friendActiveStatus = friendInfo.isFriendActive
                    )
                } else {
                    chatInfo = chatMap[id]!!.copy(
                        date = chatDetails.date,
                        message = chatDetails.message,
                        isRead = chatDetails.isRead,
                        isYourLastMsg = chatDetails.isYourLastMsg,
                    )
                }
                chatMap[id] = chatInfo
                chatMap.toMap().let {
                    _chatMapWithInfo.postValue(Resource.Success(it)) }
                getChatsUnread()
            }
        }
    }

    private fun getChatsUnread() {
        var unreadMessages = 0
        for (chat in chatMap.values) {
            if (!chat.isRead && !chat.isYourLastMsg) {
                unreadMessages++
            }
        }
        _unreadChatsNumber.postValue(unreadMessages)
    }
}