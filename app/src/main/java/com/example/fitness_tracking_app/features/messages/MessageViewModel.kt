package com.example.fitness_tracking_app.features.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracking_app.data.ChatMetadata
import com.example.fitness_tracking_app.data.MessageDataSender
import com.example.fitness_tracking_app.data.MsgData
import com.example.fitness_tracking_app.repo.ChatRepository
import com.example.fitness_tracking_app.utils.Resource
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableLiveData<Resource<Pair<ChatMetadata?, List<MessageDataSender>?>>>()
    val messages: LiveData<Resource<Pair<ChatMetadata?, List<MessageDataSender>?>>> = _messages

    private var chatListener: ValueEventListener? = null
    private var msgData: MsgData? = null

    fun sendMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            msgData?.let { chatRepository.sendMessage(it, message) }
        }
    }

    fun readChat() {
        viewModelScope.launch(Dispatchers.IO) {
            msgData?.let { chatRepository.readMessage(it.chatId) }
        }
    }

    fun fetchFromMyBase(friendId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _messages.postValue(Resource.Loading())
            val result = chatRepository.getChatFromMyDatabase(friendId)
            if (result is Resource.Success) {
                val msgData  = if (result.data != null) MsgData(friendId = friendId, chatId = result.data)
                else MsgData(friendId = friendId, chatId = UUID.randomUUID().toString())
                showChat(msgData)
            } else {
                _messages.postValue(Resource.Error())
            }
        }
    }

    private fun showChat(msgData: MsgData) {
        this.msgData = msgData
        chatListener?.let { chatRepository.removeMessagesListener(msgData.chatId, it) }
        chatListener = chatRepository.showChatWithMessages(msgData.chatId, onDataChanged = { _messages.postValue(it) })
    }

    override fun onCleared() {
        super.onCleared()
        chatListener?.let { listener ->
            msgData?.let { data -> chatRepository.removeMessagesListener(data.chatId, listener) }
        }
    }
}