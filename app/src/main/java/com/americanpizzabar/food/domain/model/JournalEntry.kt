package com.americanpizzabar.food.domain.model

import java.time.LocalDate

data class JournalEntry(
    val id: Long = 0,
    val recipeId: Long? = null,
    val recipeName: String,
    val date: LocalDate,
    val rating: Int, // 1-5
    val comment: String,
    val mood: Mood? = null,
    val healthCondition: HealthCondition? = null,
    val imageUri: String? = null,
    val tags: List<String> = emptyList(),
    val feelingAfter: FeelingAfter? = null
)

enum class Mood {
    HAPPY, NORMAL, TIRED, STRESSED, ENERGETIC, RELAXED, SAD
}

enum class HealthCondition {
    EXCELLENT, GOOD, FAIR, POOR, SICK
}

enum class FeelingAfter {
    VERY_GOOD, GOOD, NEUTRAL, SLIGHTLY_UNWELL, UNWELL
}
