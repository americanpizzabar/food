package com.americanpizzabar.food.domain.repository

import com.americanpizzabar.food.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getAllRecipes(): Flow<List<Recipe>>
    fun searchRecipes(query: String): Flow<List<Recipe>>
    suspend fun getRecipeById(id: Long): Recipe?
    suspend fun saveRecipe(recipe: Recipe): Long
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(id: Long)
}
