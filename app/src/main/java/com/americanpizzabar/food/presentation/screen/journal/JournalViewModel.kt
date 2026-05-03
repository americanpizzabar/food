package com.americanpizzabar.food.presentation.screen.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.americanpizzabar.food.domain.model.*
import com.americanpizzabar.food.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class SortOrder { DATE_DESC, DATE_ASC, RATING_DESC, RATING_ASC }

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val filterRating: Int? = null,
    val isAddingEntry: Boolean = false,
    val error: String? = null,
    // New entry form
    val newRecipeName: String = "",
    val newRating: Int = 3,
    val newComment: String = "",
    val newMood: Mood? = null,
    val newHealthCondition: HealthCondition? = null,
    val newFeelingAfter: FeelingAfter? = null,
    val newDate: LocalDate = LocalDate.now()
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init { loadEntries() }

    private fun loadEntries() {
        viewModelScope.launch {
            _uiState.value.let { state ->
                val flow = if (state.searchQuery.isBlank()) {
                    repository.getAllEntries()
                } else {
                    repository.searchEntries(state.searchQuery)
                }
                flow.collect { entries ->
                    val sorted = when (_uiState.value.sortOrder) {
                        SortOrder.DATE_DESC -> entries.sortedByDescending { it.date }
                        SortOrder.DATE_ASC -> entries.sortedBy { it.date }
                        SortOrder.RATING_DESC -> entries.sortedByDescending { it.rating }
                        SortOrder.RATING_ASC -> entries.sortedBy { it.rating }
                    }
                    val filtered = _uiState.value.filterRating?.let { r -> sorted.filter { it.rating == r } } ?: sorted
                    _uiState.update { it.copy(entries = filtered) }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadEntries()
    }

    fun updateSortOrder(order: SortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
        loadEntries()
    }

    fun setFilterRating(rating: Int?) {
        _uiState.update { it.copy(filterRating = rating) }
        loadEntries()
    }

    fun showAddEntry() { _uiState.update { it.copy(isAddingEntry = true) } }
    fun hideAddEntry() { _uiState.update { it.copy(isAddingEntry = false) } }
    fun updateNewRecipeName(name: String) { _uiState.update { it.copy(newRecipeName = name) } }
    fun updateNewRating(rating: Int) { _uiState.update { it.copy(newRating = rating) } }
    fun updateNewComment(comment: String) { _uiState.update { it.copy(newComment = comment) } }
    fun updateNewMood(mood: Mood?) { _uiState.update { it.copy(newMood = mood) } }
    fun updateNewHealthCondition(condition: HealthCondition?) { _uiState.update { it.copy(newHealthCondition = condition) } }
    fun updateNewFeelingAfter(feeling: FeelingAfter?) { _uiState.update { it.copy(newFeelingAfter = feeling) } }

    fun saveEntry() {
        val state = _uiState.value
        if (state.newRecipeName.isBlank()) return
        viewModelScope.launch {
            repository.saveEntry(JournalEntry(
                recipeName = state.newRecipeName,
                date = state.newDate,
                rating = state.newRating,
                comment = state.newComment,
                mood = state.newMood,
                healthCondition = state.newHealthCondition,
                feelingAfter = state.newFeelingAfter
            ))
            _uiState.update { it.copy(
                isAddingEntry = false,
                newRecipeName = "",
                newRating = 3,
                newComment = "",
                newMood = null,
                newHealthCondition = null,
                newFeelingAfter = null
            )}
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { repository.deleteEntry(id) }
    }
}
