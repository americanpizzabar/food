package com.americanpizzabar.food.domain.model

import java.time.LocalDate

data class MealPlan(
    val id: Long = 0,
    val date: LocalDate,
    val mealType: MealType,
    val recipeId: Long? = null,
    val recipeName: String,
    val notes: String = "",
    val isRoutine: Boolean = false,
    val routineTag: String? = null
)

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }
