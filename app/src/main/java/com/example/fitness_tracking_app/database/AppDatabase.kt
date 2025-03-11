package com.example.fitness_tracking_app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitness_tracking_app.database.dao.RunDao
import com.example.fitness_tracking_app.database.dao.UserDao
import com.example.fitness_tracking_app.database.entity.Run
import com.example.fitness_tracking_app.database.entity.User
import com.example.fitness_tracking_app.utils.GlobalStrings

@TypeConverters(MotivationConverter::class)
@Database(entities = [Run::class, User::class], version = 14)
abstract class AppDatabase : RoomDatabase() {

    abstract fun runDao(): RunDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    GlobalStrings.JOOKSMINE_DATABASE
                )
                    .fallbackToDestructiveMigration() // destructive: to change when app ready
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}