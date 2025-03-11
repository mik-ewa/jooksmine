package com.example.fitness_tracking_app.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FriendChatData(
    val friendId : String,
    val friendNickname: String,
    val friendPhoto: String,
    val chatId: String? = null,
    val isFriendActive: Boolean = false
) : Parcelable

@Parcelize
data class MsgData(
    val friendId : String,
    val chatId: String
) : Parcelable

data class FriendData(
    val username: String,
    val photo: String,
    val isFriendActive: Boolean
)