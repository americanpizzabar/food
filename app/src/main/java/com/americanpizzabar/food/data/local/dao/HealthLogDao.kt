package com.americanpizzabar.food.data.local.dao

import androidx.room.*
import com.americanpizzabar.food.data.local.entity.HealthLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthLogDao {
    @Query("SELECT * FROM health_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<HealthLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HealthLogEntity): Long

    @Query("DELETE FROM health_logs WHERE id = :id")
    suspend fun deleteLog(id: Long)
}
