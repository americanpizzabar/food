package com.americanpizzabar.food.presentation.screen.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.americanpizzabar.food.domain.model.PantryItem
import com.americanpizzabar.food.domain.repository.PantryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PantryUiState(
    val items: List<PantryItem> = emptyList(),
    val expiringItems: List<PantryItem> = emptyList(),
    val searchQuery: String = "",
    val isAddingItem: Boolean = false,
    val editingItem: PantryItem? = null,
    // form fields
    val formName: String = "",
    val formAmount: String = "",
    val formUnit: String = "",
    val formCategory: String = "その他",
    val formExpiry: LocalDate? = null,
    val formNotes: String = ""
)

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val repository: PantryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllItems(),
                repository.getExpiringItems(3)
            ) { all, expiring -> Pair(all, expiring) }
                .collect { (all, expiring) ->
                    _uiState.update { it.copy(items = all, expiringItems = expiring) }
                }
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) repository.getAllItems()
            else repository.searchItems(query)
                .collect { items -> _uiState.update { it.copy(items = items) } }
        }
    }

    fun showAddItem() {
        _uiState.update {
            it.copy(
                isAddingItem = true,
                editingItem = null,
                formName = "", formAmount = "", formUnit = "",
                formCategory = "その他", formExpiry = null, formNotes = ""
            )
        }
    }

    fun startEditItem(item: PantryItem) {
        _uiState.update {
            it.copy(
                isAddingItem = true,
                editingItem = item,
                formName = item.name,
                formAmount = item.amount,
                formUnit = item.unit,
                formCategory = item.category,
                formExpiry = item.expiryDate,
                formNotes = item.notes
            )
        }
    }

    fun hideForm() { _uiState.update { it.copy(isAddingItem = false, editingItem = null) } }

    fun updateFormName(v: String) { _uiState.update { it.copy(formName = v) } }
    fun updateFormAmount(v: String) { _uiState.update { it.copy(formAmount = v) } }
    fun updateFormUnit(v: String) { _uiState.update { it.copy(formUnit = v) } }
    fun updateFormCategory(v: String) { _uiState.update { it.copy(formCategory = v) } }
    fun updateFormExpiry(v: LocalDate?) { _uiState.update { it.copy(formExpiry = v) } }
    fun updateFormNotes(v: String) { _uiState.update { it.copy(formNotes = v) } }

    fun saveItem() {
        val state = _uiState.value
        if (state.formName.isBlank()) return
        viewModelScope.launch {
            val item = PantryItem(
                id = state.editingItem?.id ?: 0L,
                name = state.formName,
                amount = state.formAmount,
                unit = state.formUnit,
                category = state.formCategory,
                expiryDate = state.formExpiry,
                notes = state.formNotes
            )
            if (state.editingItem != null) repository.updateItem(item)
            else repository.saveItem(item)
            hideForm()
        }
    }

    fun deleteItem(id: Long) = viewModelScope.launch { repository.deleteItem(id) }
}
