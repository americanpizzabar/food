package com.americanpizzabar.food.domain.usecase

import com.americanpizzabar.food.domain.model.Recipe
import com.americanpizzabar.food.domain.model.ShoppingItem
import com.americanpizzabar.food.domain.repository.PantryRepository
import com.americanpizzabar.food.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GenerateShoppingListUseCase @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    private val pantryRepository: PantryRepository
) {
    suspend operator fun invoke(recipe: Recipe): Result<List<ShoppingItem>> = runCatching {
        val pantryItems = pantryRepository.getAllItems().first()
        val pantryNames = pantryItems.map { it.name.lowercase().trim() }

        val missingIngredients = recipe.ingredients.filter { ingredient ->
            pantryNames.none { pantry ->
                pantry.contains(ingredient.name.lowercase().trim()) ||
                    ingredient.name.lowercase().trim().contains(pantry)
            }
        }

        val shoppingItems = missingIngredients.mapIndexed { index, ingredient ->
            ShoppingItem(
                name = ingredient.name,
                amount = ingredient.amount,
                unit = ingredient.unit,
                category = ingredient.category.name,
                recipeId = recipe.id,
                recipeName = recipe.name,
                sortOrder = index
            )
        }

        shoppingRepository.saveItems(shoppingItems)
        shoppingItems
    }
}
