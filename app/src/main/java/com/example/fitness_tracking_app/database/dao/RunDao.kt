package com.example.fitness_tracking_app.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitness_tracking_app.database.entity.Run

@Dao
interface RunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(runData: Run)

    @Query("SELECT * FROM run_data ORDER BY timestamp DESC")
    fun getAllRuns(): LiveData<List<Run>>
}