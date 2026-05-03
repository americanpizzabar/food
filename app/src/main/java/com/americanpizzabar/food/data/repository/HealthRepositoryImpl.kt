package com.americanpizzabar.food.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.americanpizzabar.food.data.local.dao.HealthLogDao
import com.americanpizzabar.food.data.local.entity.HealthLogEntity
import com.americanpizzabar.food.domain.model.HealthLog
import com.americanpizzabar.food.domain.model.UserPreferences
import com.americanpizzabar.food.domain.repository.HealthRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HealthRepositoryImpl @Inject constructor(
    private val dao: HealthLogDao,
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) : HealthRepository {
    override fun getAllLogs(): Flow<List<HealthLog>> =
        dao.getAllLogs().map { it.map { e -> e.toDomain() } }

    override suspend fun saveLog(log: HealthLog): Long =
        dao.insertLog(HealthLogEntity.fromDomain(log))

    override suspend fun deleteLog(id: Long) = dao.deleteLog(id)

    override fun getUserPreferences(): Flow<UserPreferences> =
        dataStore.data.map { prefs ->
            UserPreferences(
                allergens = prefs[PK_ALLERGENS]?.let {
                    gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
                } ?: emptyList(),
                dislikedIngredients = prefs[PK_DISLIKED]?.let {
                    gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
                } ?: emptyList(),
                dietaryRestrictions = prefs[PK_DIETARY]?.let {
                    gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
                } ?: emptyList(),
                preferredCuisines = prefs[PK_CUISINES]?.let {
                    gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
                } ?: emptyList(),
                weeklyRoutines = prefs[PK_ROUTINES]?.let {
                    gson.fromJson(it, object : TypeToken<Map<String, String>>() {}.type)
                } ?: emptyMap(),
                servingSize = prefs[PK_SERVING] ?: 2,
                claudeApiKey = prefs[PK_API_KEY] ?: "",
                notificationsEnabled = prefs[PK_NOTIFICATIONS] ?: true,
                darkMode = prefs[PK_DARK_MODE] ?: false
            )
        }

    override suspend fun updateUserPreferences(prefs: UserPreferences) {
        dataStore.edit { store ->
            store[PK_ALLERGENS] = gson.toJson(prefs.allergens)
            store[PK_DISLIKED] = gson.toJson(prefs.dislikedIngredients)
            store[PK_DIETARY] = gson.toJson(prefs.dietaryRestrictions)
            store[PK_CUISINES] = gson.toJson(prefs.preferredCuisines)
            store[PK_ROUTINES] = gson.toJson(prefs.weeklyRoutines)
            store[PK_SERVING] = prefs.servingSize
            store[PK_API_KEY] = prefs.claudeApiKey
            store[PK_NOTIFICATIONS] = prefs.notificationsEnabled
            store[PK_DARK_MODE] = prefs.darkMode
        }
    }

    companion object {
        private val PK_ALLERGENS = stringPreferencesKey("allergens")
        private val PK_DISLIKED = stringPreferencesKey("disliked")
        private val PK_DIETARY = stringPreferencesKey("dietary")
        private val PK_CUISINES = stringPreferencesKey("cuisines")
        private val PK_ROUTINES = stringPreferencesKey("routines")
        private val PK_SERVING = intPreferencesKey("serving_size")
        private val PK_API_KEY = stringPreferencesKey("claude_api_key")
        private val PK_NOTIFICATIONS = booleanPreferencesKey("notifications")
        private val PK_DARK_MODE = booleanPreferencesKey("dark_mode")
    }
}
