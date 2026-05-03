package com.americanpizzabar.food.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.americanpizzabar.food.domain.model.UserPreferences
import com.americanpizzabar.food.domain.repository.HealthRepository
import com.americanpizzabar.food.domain.usecase.AnalyzeHealthCorrelationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val prefs: UserPreferences = UserPreferences(),
    val newAllergen: String = "",
    val newDisliked: String = "",
    val healthInsight: String = "",
    val isAnalyzingHealth: Boolean = false,
    val apiKeyVisible: Boolean = false,
    val savedMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val analyzeHealthCorrelation: AnalyzeHealthCorrelationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            healthRepository.getUserPreferences().collect { prefs ->
                _uiState.update { it.copy(prefs = prefs) }
            }
        }
    }

    fun addAllergen(item: String) {
        if (item.isBlank()) return
        val updated = _uiState.value.prefs.allergens + item
        updateAndSave(_uiState.value.prefs.copy(allergens = updated))
        _uiState.update { it.copy(newAllergen = "") }
    }

    fun removeAllergen(item: String) {
        val updated = _uiState.value.prefs.allergens - item
        updateAndSave(_uiState.value.prefs.copy(allergens = updated))
    }

    fun addDisliked(item: String) {
        if (item.isBlank()) return
        val updated = _uiState.value.prefs.dislikedIngredients + item
        updateAndSave(_uiState.value.prefs.copy(dislikedIngredients = updated))
        _uiState.update { it.copy(newDisliked = "") }
    }

    fun removeDisliked(item: String) {
        val updated = _uiState.value.prefs.dislikedIngredients - item
        updateAndSave(_uiState.value.prefs.copy(dislikedIngredients = updated))
    }

    fun toggleDietaryRestriction(restriction: String) {
        val current = _uiState.value.prefs.dietaryRestrictions
        val updated = if (restriction in current) current - restriction else current + restriction
        updateAndSave(_uiState.value.prefs.copy(dietaryRestrictions = updated))
    }

    fun updateApiKey(key: String) {
        _uiState.update { it.copy(prefs = it.prefs.copy(claudeApiKey = key)) }
    }

    fun saveApiKey() {
        updateAndSave(_uiState.value.prefs)
        _uiState.update { it.copy(savedMessage = "APIキーを保存しました") }
    }

    fun toggleDarkMode() {
        val updated = _uiState.value.prefs.copy(darkMode = !_uiState.value.prefs.darkMode)
        updateAndSave(updated)
    }

    fun toggleNotifications() {
        val updated = _uiState.value.prefs.copy(
            notificationsEnabled = !_uiState.value.prefs.notificationsEnabled
        )
        updateAndSave(updated)
    }

    fun updateServingSize(size: Int) {
        val updated = _uiState.value.prefs.copy(servingSize = size)
        updateAndSave(updated)
    }

    fun updateNewAllergen(v: String) { _uiState.update { it.copy(newAllergen = v) } }
    fun updateNewDisliked(v: String) { _uiState.update { it.copy(newDisliked = v) } }
    fun toggleApiKeyVisible() { _uiState.update { it.copy(apiKeyVisible = !it.apiKeyVisible) } }
    fun clearSavedMessage() { _uiState.update { it.copy(savedMessage = null) } }

    fun runHealthAnalysis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingHealth = true) }
            analyzeHealthCorrelation()
                .onSuccess { insight -> _uiState.update { it.copy(healthInsight = insight, isAnalyzingHealth = false) } }
                .onFailure { _uiState.update { it.copy(isAnalyzingHealth = false) } }
        }
    }

    private fun updateAndSave(prefs: UserPreferences) {
        _uiState.update { it.copy(prefs = prefs) }
        viewModelScope.launch { healthRepository.updateUserPreferences(prefs) }
    }
}
