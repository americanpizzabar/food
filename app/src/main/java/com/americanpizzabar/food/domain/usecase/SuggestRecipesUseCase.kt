package com.americanpizzabar.food.domain.usecase

import com.americanpizzabar.food.data.remote.ClaudeAiClient
import com.americanpizzabar.food.domain.model.Recipe
import com.americanpizzabar.food.domain.repository.HealthRepository
import com.americanpizzabar.food.domain.repository.PantryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SuggestRecipesUseCase @Inject constructor(
    private val client: ClaudeAiClient,
    private val pantryRepository: PantryRepository,
    private val healthRepository: HealthRepository
) {
    suspend operator fun invoke(
        mood: String = "",
        craving: String = "",
        healthCondition: String = "",
        nutritionNeeds: String = ""
    ): Result<List<Recipe>> = runCatching {
        val pantryItems = pantryRepository.getAllItems().first().map { it.name }
        val prefs = healthRepository.getUserPreferences().first()
        client.suggestRecipes(
            mood = mood,
            craving = craving,
            healthCondition = healthCondition,
            nutritionNeeds = nutritionNeeds,
            pantryItems = pantryItems,
            allergens = prefs.allergens,
            dislikedIngredients = prefs.dislikedIngredients
        )
    }
}
