package com.americanpizzabar.food.data.repository

import com.americanpizzabar.food.data.local.dao.RecipeDao
import com.americanpizzabar.food.data.local.entity.RecipeEntity
import com.americanpizzabar.food.domain.model.Recipe
import com.americanpizzabar.food.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val dao: RecipeDao
) : RecipeRepository {
    override fun getAllRecipes(): Flow<List<Recipe>> =
        dao.getAllRecipes().map { it.map { e -> e.toDomain() } }

    override fun searchRecipes(query: String): Flow<List<Recipe>> =
        dao.searchRecipes(query).map { it.map { e -> e.toDomain() } }

    override suspend fun getRecipeById(id: Long): Recipe? =
        dao.getRecipeById(id)?.toDomain()

    override suspend fun saveRecipe(recipe: Recipe): Long =
        dao.insertRecipe(RecipeEntity.fromDomain(recipe))

    override suspend fun updateRecipe(recipe: Recipe) =
        dao.updateRecipe(RecipeEntity.fromDomain(recipe))

    override suspend fun deleteRecipe(id: Long) = dao.deleteRecipe(id)
}
