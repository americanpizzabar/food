package com.americanpizzabar.food.presentation.screen.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.americanpizzabar.food.domain.model.MealPlan
import com.americanpizzabar.food.domain.model.MealType
import com.americanpizzabar.food.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class CalendarUiState(
    val weekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val mealPlans: List<MealPlan> = emptyList(),
    val routines: List<MealPlan> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingPlan: MealPlan? = null,
    val dialogDate: LocalDate = LocalDate.now(),
    val dialogMealType: MealType = MealType.DINNER,
    val dialogRecipeName: String = "",
    val dialogNotes: String = "",
    val dialogIsRoutine: Boolean = false,
    val dialogRoutineTag: String = "",
    val showRoutineDialog: Boolean = false,
    val routineInput: Map<String, String> = emptyMap()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadWeek()
        viewModelScope.launch {
            repository.getRoutines().collect { routines ->
                _uiState.update { it.copy(routines = routines) }
            }
        }
    }

    private fun loadWeek() {
        viewModelScope.launch {
            repository.getMealPlansForWeek(_uiState.value.weekStart).collect { plans ->
                _uiState.update { it.copy(mealPlans = plans) }
            }
        }
    }

    fun previousWeek() {
        _uiState.update { it.copy(weekStart = it.weekStart.minusWeeks(1)) }
        loadWeek()
    }

    fun nextWeek() {
        _uiState.update { it.copy(weekStart = it.weekStart.plusWeeks(1)) }
        loadWeek()
    }

    fun goToToday() {
        _uiState.update {
            it.copy(weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
        }
        loadWeek()
    }

    fun showAddDialog(date: LocalDate, mealType: MealType) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                editingPlan = null,
                dialogDate = date,
                dialogMealType = mealType,
                dialogRecipeName = "",
                dialogNotes = "",
                dialogIsRoutine = false,
                dialogRoutineTag = ""
            )
        }
    }

    fun showEditDialog(plan: MealPlan) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                editingPlan = plan,
                dialogDate = plan.date,
                dialogMealType = plan.mealType,
                dialogRecipeName = plan.recipeName,
                dialogNotes = plan.notes,
                dialogIsRoutine = plan.isRoutine,
                dialogRoutineTag = plan.routineTag ?: ""
            )
        }
    }

    fun hideAddDialog() { _uiState.update { it.copy(showAddDialog = false, editingPlan = null) } }

    fun updateDialogRecipeName(v: String) { _uiState.update { it.copy(dialogRecipeName = v) } }
    fun updateDialogNotes(v: String) { _uiState.update { it.copy(dialogNotes = v) } }
    fun updateDialogMealType(v: MealType) { _uiState.update { it.copy(dialogMealType = v) } }
    fun updateDialogIsRoutine(v: Boolean) { _uiState.update { it.copy(dialogIsRoutine = v) } }
    fun updateDialogRoutineTag(v: String) { _uiState.update { it.copy(dialogRoutineTag = v) } }

    fun saveMealPlan() {
        val state = _uiState.value
        if (state.dialogRecipeName.isBlank()) return
        viewModelScope.launch {
            val plan = MealPlan(
                id = state.editingPlan?.id ?: 0L,
                date = state.dialogDate,
                mealType = state.dialogMealType,
                recipeName = state.dialogRecipeName,
                notes = state.dialogNotes,
                isRoutine = state.dialogIsRoutine,
                routineTag = state.dialogRoutineTag.takeIf { it.isNotBlank() }
            )
            if (state.editingPlan != null) repository.updateMealPlan(plan)
            else repository.saveMealPlan(plan)
            hideAddDialog()
        }
    }

    fun deleteMealPlan(id: Long) = viewModelScope.launch { repository.deleteMealPlan(id) }

    fun showRoutineDialog() {
        val routineMap = _uiState.value.routines
            .groupBy { it.routineTag ?: "" }
            .mapValues { it.value.firstOrNull()?.recipeName ?: "" }
        _uiState.update { it.copy(showRoutineDialog = true, routineInput = routineMap) }
    }

    fun hideRoutineDialog() { _uiState.update { it.copy(showRoutineDialog = false) } }

    fun updateRoutineInput(tag: String, value: String) {
        _uiState.update { it.copy(routineInput = it.routineInput + (tag to value)) }
    }
}
