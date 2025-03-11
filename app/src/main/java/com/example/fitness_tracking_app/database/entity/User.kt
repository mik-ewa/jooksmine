package com.example.fitness_tracking_app.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitness_tracking_app.database.Motivation
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "user_data")
@Parcelize
data class User (
    @PrimaryKey(autoGenerate = false) val uid: String,
    val username: String? = null,
    val name: String? = null,
    val profilePhoto: String? = null,
    val motivation: Motivation = Motivation("NONE", null)
):Parcelable