package com.americanpizzabar.food.domain.repository

import com.americanpizzabar.food.domain.model.HealthLog
import com.americanpizzabar.food.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface HealthRepository {
    fun getAllLogs(): Flow<List<HealthLog>>
    suspend fun saveLog(log: HealthLog): Long
    suspend fun deleteLog(id: Long)
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateUserPreferences(prefs: UserPreferences)
}
