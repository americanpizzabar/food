package com.americanpizzabar.food.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.americanpizzabar.food.domain.model.Difficulty
import com.americanpizzabar.food.domain.model.Ingredient
import com.americanpizzabar.food.domain.model.NutritionInfo
import com.americanpizzabar.food.domain.model.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val ingredientsJson: String,
    val stepsJson: String,
    val calories: Int,
    val nutritionJson: String,
    val tagsJson: String,
    val imageUri: String? = null,
    val referenceImageUrl: String? = null,
    val sourceName: String? = null,
    val sourceUrl: String? = null,
    val servings: Int = 2,
    val cookTimeMinutes: Int = 30,
    val prepTimeMinutes: Int = 15,
    val difficulty: String = "MEDIUM",
    val platingAdvice: String? = null,
    val remakeIdeasJson: String = "[]",
    val createdAt: String = LocalDate.now().toString()
) {
    fun toDomain(): Recipe {
        val gson = Gson()
        return Recipe(
            id = id,
            name = name,
            description = description,
            ingredients = gson.fromJson(ingredientsJson, object : TypeToken<List<Ingredient>>() {}.type),
            steps = gson.fromJson(stepsJson, object : TypeToken<List<String>>() {}.type),
            calories = calories,
            nutrition = gson.fromJson(nutritionJson, NutritionInfo::class.java),
            tags = gson.fromJson(tagsJson, object : TypeToken<List<String>>() {}.type),
            imageUri = imageUri,
            referenceImageUrl = referenceImageUrl,
            sourceName = sourceName,
            sourceUrl = sourceUrl,
            servings = servings,
            cookTimeMinutes = cookTimeMinutes,
            prepTimeMinutes = prepTimeMinutes,
            difficulty = Difficulty.valueOf(difficulty),
            platingAdvice = platingAdvice,
            remakeIdeas = gson.fromJson(remakeIdeasJson, object : TypeToken<List<String>>() {}.type),
            createdAt = LocalDate.parse(createdAt)
        )
    }

    companion object {
        fun fromDomain(recipe: Recipe): RecipeEntity {
            val gson = Gson()
            return RecipeEntity(
                id = recipe.id,
                name = recipe.name,
                description = recipe.description,
                ingredientsJson = gson.toJson(recipe.ingredients),
                stepsJson = gson.toJson(recipe.steps),
                calories = recipe.calories,
                nutritionJson = gson.toJson(recipe.nutrition),
                tagsJson = gson.toJson(recipe.tags),
                imageUri = recipe.imageUri,
                referenceImageUrl = recipe.referenceImageUrl,
                sourceName = recipe.sourceName,
                sourceUrl = recipe.sourceUrl,
                servings = recipe.servings,
                cookTimeMinutes = recipe.cookTimeMinutes,
                prepTimeMinutes = recipe.prepTimeMinutes,
                difficulty = recipe.difficulty.name,
                platingAdvice = recipe.platingAdvice,
                remakeIdeasJson = gson.toJson(recipe.remakeIdeas),
                createdAt = recipe.createdAt.toString()
            )
        }
    }
}
