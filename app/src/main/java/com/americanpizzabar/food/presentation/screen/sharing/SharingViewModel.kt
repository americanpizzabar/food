package com.americanpizzabar.food.presentation.screen.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.americanpizzabar.food.data.remote.ClaudeAiClient
import com.americanpizzabar.food.domain.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SharingUiState(
    val recipe: Recipe? = null,
    val caption: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val isGenerating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SharingViewModel @Inject constructor(
    private val claudeClient: ClaudeAiClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharingUiState())
    val uiState: StateFlow<SharingUiState> = _uiState.asStateFlow()

    fun setRecipe(recipe: Recipe) { _uiState.update { it.copy(recipe = recipe) } }
    fun updateComment(comment: String) { _uiState.update { it.copy(comment = comment) } }
    fun updateRating(rating: Int) { _uiState.update { it.copy(rating = rating) } }

    fun generateCaption() {
        val recipe = _uiState.value.recipe ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            try {
                val caption = claudeClient.generateShareCardContent(
                    recipe, _uiState.value.comment, _uiState.value.rating
                )
                _uiState.update { it.copy(isGenerating = false, caption = caption) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, error = e.message) }
            }
        }
    }
}
