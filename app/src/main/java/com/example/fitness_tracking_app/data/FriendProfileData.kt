package com.example.fitness_tracking_app.data

data class FriendBaseProfileData(
    val friendId: String? = null,
    val friendName: String? = null,
    val friendUsername: String? = null,
    val friendImage: String? = null,
    val friendActivityData : List<String>? = null,
    val friendBackgroundImage: String? = null,
)
