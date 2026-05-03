package com.americanpizzabar.food.domain.model

import java.time.LocalDate

data class Recipe(
    val id: Long = 0,
    val name: String,
    val description: String,
    val ingredients: List<Ingredient>,
    val steps: List<String>,
    val calories: Int,
    val nutrition: NutritionInfo,
    val tags: List<String>,
    val imageUri: String? = null,
    val referenceImageUrl: String? = null,
    val sourceName: String? = null,
    val sourceUrl: String? = null,
    val servings: Int = 2,
    val cookTimeMinutes: Int = 30,
    val prepTimeMinutes: Int = 15,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val platingAdvice: String? = null,
    val remakeIdeas: List<String> = emptyList(),
    val createdAt: LocalDate = LocalDate.now()
)

data class Ingredient(
    val name: String,
    val amount: String,
    val unit: String,
    val calories: Int = 0,
    val isAllergen: Boolean = false,
    val category: IngredientCategory = IngredientCategory.OTHER
)

data class NutritionInfo(
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val fiber: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f,
    val vitamins: Map<String, Float> = emptyMap(),
    val minerals: Map<String, Float> = emptyMap()
)

enum class Difficulty { EASY, MEDIUM, HARD }

enum class IngredientCategory {
    VEGETABLE, MEAT, FISH, DAIRY, GRAIN, SPICE, SAUCE, FRUIT, OTHER
}
