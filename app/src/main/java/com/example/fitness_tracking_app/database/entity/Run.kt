package com.example.fitness_tracking_app.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_data")
data class Run(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timeInSeconds: Int,
    val distance: Double,
    val pace: String,
    val timestamp: Long = System.currentTimeMillis()
)