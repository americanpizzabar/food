package com.americanpizzabar.food.domain.model

import java.time.LocalDate

data class HealthLog(
    val id: Long = 0,
    val date: LocalDate,
    val journalEntryId: Long,
    val recipeName: String,
    val feelingBefore: Int, // 1-5
    val feelingAfter: Int,  // 1-5
    val energyLevel: Int,   // 1-5
    val digestiveComfort: Int, // 1-5
    val notes: String = ""
)

data class UserPreferences(
    val allergens: List<String> = emptyList(),
    val dislikedIngredients: List<String> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList(),
    val preferredCuisines: List<String> = emptyList(),
    val weeklyRoutines: Map<String, String> = emptyMap(), // dayOfWeek -> routine description
    val servingSize: Int = 2,
    val claudeApiKey: String = "",
    val notificationsEnabled: Boolean = true,
    val darkMode: Boolean = false
)
