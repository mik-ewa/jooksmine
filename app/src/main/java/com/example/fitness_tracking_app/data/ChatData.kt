package com.example.fitness_tracking_app.data

data class ChatData(
    val chatMetadata: ChatMetadata? = null,
    val messages: List<MessageData>? = null,
)

data class ChatMetadata(
    val lastRead: Long = 0,
    val chatId: String? = null,
    val participants: Map<String, Long>? = null,
)

data class ChatInfo(
    val chatId: String,
    val date: Long,
    val message: String,
    val isRead: Boolean,
    val isYourLastMsg: Boolean,
    val friendId: String,
    val friendNickname: String?= null,
    val friendPhoto: String?= null,
    val friendActiveStatus: Boolean = false
)

data class ChatDetails(
    val friendId: String,
    val chatId: String,
    val date: Long,
    val message: String,
    val isRead: Boolean,
    val isYourLastMsg: Boolean,
)