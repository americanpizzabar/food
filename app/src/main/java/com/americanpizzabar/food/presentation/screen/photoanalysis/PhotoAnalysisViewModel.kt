package com.americanpizzabar.food.presentation.screen.photoanalysis

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.americanpizzabar.food.domain.model.Ingredient
import com.americanpizzabar.food.domain.model.Recipe
import com.americanpizzabar.food.domain.repository.RecipeRepository
import com.americanpizzabar.food.domain.usecase.AnalyzeFoodPhotoUseCase
import com.americanpizzabar.food.domain.usecase.GenerateShoppingListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoAnalysisUiState(
    val isAnalyzing: Boolean = false,
    val analyzedRecipe: Recipe? = null,
    val editedRecipe: Recipe? = null,
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val shoppingListGenerated: Boolean = false
)

@HiltViewModel
class PhotoAnalysisViewModel @Inject constructor(
    private val analyzePhoto: AnalyzeFoodPhotoUseCase,
    private val recipeRepository: RecipeRepository,
    private val generateShoppingList: GenerateShoppingListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoAnalysisUiState())
    val uiState: StateFlow<PhotoAnalysisUiState> = _uiState.asStateFlow()

    fun analyzePhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, error = null) }
            analyzePhoto.invoke(bitmap)
                .onSuccess { recipe ->
                    _uiState.update { it.copy(
                        isAnalyzing = false,
                        analyzedRecipe = recipe,
                        editedRecipe = recipe
                    )}
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isAnalyzing = false, error = e.message) }
                }
        }
    }

    fun updateRecipeName(name: String) {
        _uiState.update { it.copy(editedRecipe = it.editedRecipe?.copy(name = name)) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(editedRecipe = it.editedRecipe?.copy(description = description)) }
    }

    fun updateCalories(calories: String) {
        calories.toIntOrNull()?.let { cal ->
            _uiState.update { it.copy(editedRecipe = it.editedRecipe?.copy(calories = cal)) }
        }
    }

    fun updateIngredient(index: Int, ingredient: Ingredient) {
        val recipe = _uiState.value.editedRecipe ?: return
        val newIngredients = recipe.ingredients.toMutableList()
        if (index < newIngredients.size) newIngredients[index] = ingredient
        _uiState.update { it.copy(editedRecipe = recipe.copy(ingredients = newIngredients)) }
    }

    fun addIngredient(ingredient: Ingredient) {
        val recipe = _uiState.value.editedRecipe ?: return
        _uiState.update { it.copy(editedRecipe = recipe.copy(
            ingredients = recipe.ingredients + ingredient
        ))}
    }

    fun removeIngredient(index: Int) {
        val recipe = _uiState.value.editedRecipe ?: return
        val newIngredients = recipe.ingredients.toMutableList().also { it.removeAt(index) }
        _uiState.update { it.copy(editedRecipe = recipe.copy(ingredients = newIngredients)) }
    }

    fun updateStep(index: Int, step: String) {
        val recipe = _uiState.value.editedRecipe ?: return
        val newSteps = recipe.steps.toMutableList()
        if (index < newSteps.size) newSteps[index] = step
        _uiState.update { it.copy(editedRecipe = recipe.copy(steps = newSteps)) }
    }

    fun addStep(step: String) {
        val recipe = _uiState.value.editedRecipe ?: return
        _uiState.update { it.copy(editedRecipe = recipe.copy(steps = recipe.steps + step)) }
    }

    fun removeStep(index: Int) {
        val recipe = _uiState.value.editedRecipe ?: return
        val newSteps = recipe.steps.toMutableList().also { it.removeAt(index) }
        _uiState.update { it.copy(editedRecipe = recipe.copy(steps = newSteps)) }
    }

    fun saveRecipe() {
        val recipe = _uiState.value.editedRecipe ?: return
        viewModelScope.launch {
            recipeRepository.saveRecipe(recipe)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun generateShoppingList() {
        val recipe = _uiState.value.editedRecipe ?: return
        viewModelScope.launch {
            generateShoppingList.invoke(recipe).onSuccess {
                _uiState.update { it.copy(shoppingListGenerated = true) }
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
