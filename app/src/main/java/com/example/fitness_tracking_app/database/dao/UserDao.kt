package com.example.fitness_tracking_app.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitness_tracking_app.database.Motivation
import com.example.fitness_tracking_app.database.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(userData: User)

    @Query("SELECT * FROM user_data WHERE uid=:userId")
    suspend fun getUser(userId: String) : User?

    @Query("SELECT * FROM user_data WHERE uid=:userId")
    fun getUserFlow(userId: String) : Flow<User?>

    @Query("UPDATE user_data SET username=:username WHERE uid=:userId")
    fun updateUsername(userId: String, username: String)

    @Query("UPDATE user_data SET name=:name WHERE uid=:userId")
    fun updateName(userId: String, name: String)

    @Query("UPDATE user_data SET motivation=:motivation WHERE uid=:userId")
    suspend fun updateMotivation(userId: String, motivation: Motivation)

    @Query("UPDATE user_data SET profilePhoto=:profilePhoto WHERE uid=:userId")
    fun updateProfilePhoto(userId: String, profilePhoto: String)
}