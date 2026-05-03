package com.americanpizzabar.food.domain.repository

import com.americanpizzabar.food.domain.model.MealPlan
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CalendarRepository {
    fun getMealPlansForWeek(startDate: LocalDate): Flow<List<MealPlan>>
    fun getMealPlansForDate(date: LocalDate): Flow<List<MealPlan>>
    fun getRoutines(): Flow<List<MealPlan>>
    suspend fun saveMealPlan(plan: MealPlan): Long
    suspend fun updateMealPlan(plan: MealPlan)
    suspend fun deleteMealPlan(id: Long)
}
