package com.example.fitness_tracking_app.base

import com.example.fitness_tracking_app.R

sealed class DataState <out T: Any> {

    object Loading : DataState<Nothing>()

    data class Success <out T: Any> (val data: T) : DataState<T>()

    data class Error(val message: Message = Message.MessageInfo(R.string.error_generic)) : DataState<Nothing>()
}

sealed class Message {
    class MessageInfo(val message: Int) : Message()
}
