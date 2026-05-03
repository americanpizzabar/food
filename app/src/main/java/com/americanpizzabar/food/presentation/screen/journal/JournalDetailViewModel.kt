package com.americanpizzabar.food.presentation.screen.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.americanpizzabar.food.domain.model.*
import com.americanpizzabar.food.domain.repository.HealthRepository
import com.americanpizzabar.food.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class JournalDetailUiState(
    val entry: JournalEntry? = null,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null,
    // Edit fields
    val editRecipeName: String = "",
    val editRating: Int = 3,
    val editComment: String = "",
    val editMood: Mood? = null,
    val editHealthCondition: HealthCondition? = null,
    val editFeelingAfter: FeelingAfter? = null,
    // Health log form
    val showHealthLogForm: Boolean = false,
    val healthLogFeelingBefore: Int = 3,
    val healthLogFeelingAfter: Int = 3,
    val healthLogEnergyLevel: Int = 3,
    val healthLogDigestiveComfort: Int = 3,
    val healthLogNotes: String = "",
    val isHealthLogSaved: Boolean = false
)

@HiltViewModel
class JournalDetailViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val healthRepository: HealthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryId: Long = savedStateHandle.get<String>("entryId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(JournalDetailUiState())
    val uiState: StateFlow<JournalDetailUiState> = _uiState.asStateFlow()

    init {
        loadEntry()
    }

    private fun loadEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val entry = journalRepository.getEntryById(entryId)
            if (entry != null) {
                _uiState.update {
                    it.copy(
                        entry = entry,
                        isLoading = false,
                        editRecipeName = entry.recipeName,
                        editRating = entry.rating,
                        editComment = entry.comment,
                        editMood = entry.mood,
                        editHealthCondition = entry.healthCondition,
                        editFeelingAfter = entry.feelingAfter
                    )
                }
                checkHealthLog(entry.id)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "日記が見つかりません") }
            }
        }
    }

    private fun checkHealthLog(journalEntryId: Long) {
        viewModelScope.launch {
            healthRepository.getAllLogs().take(1).collect { logs ->
                val hasLog = logs.any { it.journalEntryId == journalEntryId }
                _uiState.update { it.copy(showHealthLogForm = !hasLog) }
            }
        }
    }

    fun enterEditMode() { _uiState.update { it.copy(isEditing = true) } }

    fun exitEditMode() {
        val entry = _uiState.value.entry ?: return
        _uiState.update {
            it.copy(
                isEditing = false,
                editRecipeName = entry.recipeName,
                editRating = entry.rating,
                editComment = entry.comment,
                editMood = entry.mood,
                editHealthCondition = entry.healthCondition,
                editFeelingAfter = entry.feelingAfter
            )
        }
    }

    fun updateEditRecipeName(name: String) { _uiState.update { it.copy(editRecipeName = name) } }
    fun updateEditRating(rating: Int) { _uiState.update { it.copy(editRating = rating) } }
    fun updateEditComment(comment: String) { _uiState.update { it.copy(editComment = comment) } }
    fun updateEditMood(mood: Mood?) { _uiState.update { it.copy(editMood = mood) } }
    fun updateEditHealthCondition(condition: HealthCondition?) { _uiState.update { it.copy(editHealthCondition = condition) } }
    fun updateEditFeelingAfter(feeling: FeelingAfter?) { _uiState.update { it.copy(editFeelingAfter = feeling) } }

    fun saveChanges() {
        val state = _uiState.value
        val entry = state.entry ?: return
        if (state.editRecipeName.isBlank()) return
        viewModelScope.launch {
            val updated = entry.copy(
                recipeName = state.editRecipeName,
                rating = state.editRating,
                comment = state.editComment,
                mood = state.editMood,
                healthCondition = state.editHealthCondition,
                feelingAfter = state.editFeelingAfter
            )
            journalRepository.updateEntry(updated)
            _uiState.update { it.copy(entry = updated, isEditing = false, isSaved = true) }
        }
    }

    fun deleteEntry() {
        viewModelScope.launch {
            journalRepository.deleteEntry(entryId)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    // Health log form
    fun updateHealthLogFeelingBefore(value: Int) { _uiState.update { it.copy(healthLogFeelingBefore = value) } }
    fun updateHealthLogFeelingAfter(value: Int) { _uiState.update { it.copy(healthLogFeelingAfter = value) } }
    fun updateHealthLogEnergyLevel(value: Int) { _uiState.update { it.copy(healthLogEnergyLevel = value) } }
    fun updateHealthLogDigestiveComfort(value: Int) { _uiState.update { it.copy(healthLogDigestiveComfort = value) } }
    fun updateHealthLogNotes(notes: String) { _uiState.update { it.copy(healthLogNotes = notes) } }

    fun saveHealthLog() {
        val state = _uiState.value
        val entry = state.entry ?: return
        viewModelScope.launch {
            healthRepository.saveLog(
                com.americanpizzabar.food.domain.model.HealthLog(
                    date = entry.date,
                    journalEntryId = entry.id,
                    recipeName = entry.recipeName,
                    feelingBefore = state.healthLogFeelingBefore,
                    feelingAfter = state.healthLogFeelingAfter,
                    energyLevel = state.healthLogEnergyLevel,
                    digestiveComfort = state.healthLogDigestiveComfort,
                    notes = state.healthLogNotes
                )
            )
            _uiState.update { it.copy(showHealthLogForm = false, isHealthLogSaved = true) }
        }
    }
}
