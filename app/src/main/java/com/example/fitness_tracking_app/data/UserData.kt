package com.example.fitness_tracking_app.data

data class UserData(
    val uid: String? = null,
    val username: String?= null,
    val email: String?= null,
    val name: String?= null,
    val profilePhoto: String?= null,
    val backgroundPhoto: String?= null,
    val runsList: List<String>? = null,
    val jointRunsMap: Map<String, List<String>>? = null,
    val friends: Map<String, FriendshipStatus>? = null,
    val personalData: PersonalData?= null,
    val messagesMap: Map<String, String>? = null //key - friendId, value - chatId
)
