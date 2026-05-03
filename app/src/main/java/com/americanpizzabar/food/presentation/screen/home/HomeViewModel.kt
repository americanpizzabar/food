package com.americanpizzabar.food.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.americanpizzabar.food.domain.model.MealPlan
import com.americanpizzabar.food.domain.model.PantryItem
import com.americanpizzabar.food.domain.repository.CalendarRepository
import com.americanpizzabar.food.domain.repository.PantryRepository
import com.americanpizzabar.food.domain.repository.ShoppingRepository
import com.americanpizzabar.food.domain.usecase.AnalyzeHealthCorrelationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val todayMeals: List<MealPlan> = emptyList(),
    val expiringItems: List<PantryItem> = emptyList(),
    val shoppingCount: Int = 0,
    val pantryCount: Int = 0,
    val healthInsight: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val pantryRepository: PantryRepository,
    private val shoppingRepository: ShoppingRepository,
    private val analyzeHealthCorrelation: AnalyzeHealthCorrelationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
        loadHealthInsight()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                calendarRepository.getMealPlansForDate(LocalDate.now()),
                pantryRepository.getExpiringItems(3),
                shoppingRepository.getUncheckedItems(),
                pantryRepository.getAllItems()
            ) { meals, expiring, shopping, pantry ->
                HomeUiState(
                    todayMeals = meals,
                    expiringItems = expiring,
                    shoppingCount = shopping.size,
                    pantryCount = pantry.size
                )
            }.collect { state ->
                _uiState.update { it.copy(
                    todayMeals = state.todayMeals,
                    expiringItems = state.expiringItems,
                    shoppingCount = state.shoppingCount,
                    pantryCount = state.pantryCount
                )}
            }
        }
    }

    private fun loadHealthInsight() {
        viewModelScope.launch {
            analyzeHealthCorrelation().onSuccess { insight ->
                _uiState.update { it.copy(healthInsight = insight) }
            }
        }
    }
}
