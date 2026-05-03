package com.americanpizzabar.food.presentation.screen.recipesuggestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.americanpizzabar.food.domain.model.Mood
import com.americanpizzabar.food.domain.model.Recipe
import com.americanpizzabar.food.domain.repository.RecipeRepository
import com.americanpizzabar.food.domain.usecase.GenerateShoppingListUseCase
import com.americanpizzabar.food.domain.usecase.SuggestRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SuggestionUiState(
    val mood: String = "",
    val craving: String = "",
    val healthCondition: String = "",
    val nutritionNeeds: String = "",
    val suggestions: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val savedRecipeId: Long? = null,
    val shoppingListGenerated: Boolean = false
)

@HiltViewModel
class RecipeSuggestionViewModel @Inject constructor(
    private val suggestRecipes: SuggestRecipesUseCase,
    private val recipeRepository: RecipeRepository,
    private val generateShoppingList: GenerateShoppingListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuggestionUiState())
    val uiState: StateFlow<SuggestionUiState> = _uiState.asStateFlow()

    fun updateMood(mood: String) { _uiState.update { it.copy(mood = mood) } }
    fun updateCraving(craving: String) { _uiState.update { it.copy(craving = craving) } }
    fun updateHealthCondition(condition: String) { _uiState.update { it.copy(healthCondition = condition) } }
    fun updateNutritionNeeds(needs: String) { _uiState.update { it.copy(nutritionNeeds = needs) } }

    fun getSuggestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val state = _uiState.value
            suggestRecipes(
                mood = state.mood,
                craving = state.craving,
                healthCondition = state.healthCondition,
                nutritionNeeds = state.nutritionNeeds
            ).onSuccess { recipes ->
                _uiState.update { it.copy(isLoading = false, suggestions = recipes) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            val id = recipeRepository.saveRecipe(recipe)
            _uiState.update { it.copy(savedRecipeId = id) }
        }
    }

    fun addToShoppingList(recipe: Recipe) {
        viewModelScope.launch {
            generateShoppingList(recipe).onSuccess {
                _uiState.update { it.copy(shoppingListGenerated = true) }
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
