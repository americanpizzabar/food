package com.americanpizzabar.food.presentation.screen.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.americanpizzabar.food.domain.model.ShoppingItem
import com.americanpizzabar.food.domain.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingUiState(
    val items: List<ShoppingItem> = emptyList(),
    val showChecked: Boolean = true,
    val isAddingItem: Boolean = false,
    val newItemName: String = "",
    val newItemAmount: String = "",
    val newItemUnit: String = "",
    val newItemCategory: String = "その他",
    val showDeleteAllDialog: Boolean = false
)

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingUiState())
    val uiState: StateFlow<ShoppingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllItems().collect { items ->
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    fun toggleChecked(id: Long) = viewModelScope.launch { repository.toggleChecked(id) }
    fun deleteItem(id: Long) = viewModelScope.launch { repository.deleteItem(id) }
    fun deleteCheckedItems() = viewModelScope.launch { repository.deleteCheckedItems() }
    fun deleteAllItems() = viewModelScope.launch {
        repository.deleteAllItems()
        _uiState.update { it.copy(showDeleteAllDialog = false) }
    }

    fun showAddItem() { _uiState.update { it.copy(isAddingItem = true) } }
    fun hideAddItem() { _uiState.update { it.copy(isAddingItem = false) } }
    fun updateNewName(name: String) { _uiState.update { it.copy(newItemName = name) } }
    fun updateNewAmount(amount: String) { _uiState.update { it.copy(newItemAmount = amount) } }
    fun updateNewUnit(unit: String) { _uiState.update { it.copy(newItemUnit = unit) } }
    fun updateNewCategory(category: String) { _uiState.update { it.copy(newItemCategory = category) } }
    fun toggleShowChecked() { _uiState.update { it.copy(showChecked = !it.showChecked) } }
    fun showDeleteAllDialog() { _uiState.update { it.copy(showDeleteAllDialog = true) } }
    fun hideDeleteAllDialog() { _uiState.update { it.copy(showDeleteAllDialog = false) } }

    fun saveItem() {
        val state = _uiState.value
        if (state.newItemName.isBlank()) return
        viewModelScope.launch {
            repository.saveItem(
                ShoppingItem(
                    name = state.newItemName,
                    amount = state.newItemAmount,
                    unit = state.newItemUnit,
                    category = state.newItemCategory
                )
            )
            _uiState.update {
                it.copy(
                    isAddingItem = false,
                    newItemName = "",
                    newItemAmount = "",
                    newItemUnit = "",
                    newItemCategory = "その他"
                )
            }
        }
    }
}
