package com.example.fitness_tracking_app.data

data class MessageData(
    val message: String? = null,
    val senderId: String? = null,
    val imageURL: String? = null,
    val timeStamp: Long = 0,
)

data class MessageDataSender(
    val message: String? = null,
    val sender: Boolean? = null,
    val imageURL: String? = null,
    val timeStamp: Long = 0,
)