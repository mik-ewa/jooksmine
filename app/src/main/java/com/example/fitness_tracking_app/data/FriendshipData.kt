package com.example.fitness_tracking_app.data

data class FriendshipData (
    val friendID: String,
    val friendName: String,
    val friendNickname: String,
    val friendImage: String,
    val friendStatus: FriendshipStatus
)

enum class FriendshipStatus {
    ACCEPTED,
    PENDING,
    REQUEST
}