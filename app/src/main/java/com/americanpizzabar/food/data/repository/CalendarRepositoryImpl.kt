package com.americanpizzabar.food.data.repository

import com.americanpizzabar.food.data.local.dao.MealPlanDao
import com.americanpizzabar.food.data.local.entity.MealPlanEntity
import com.americanpizzabar.food.domain.model.MealPlan
import com.americanpizzabar.food.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val dao: MealPlanDao
) : CalendarRepository {
    override fun getMealPlansForWeek(startDate: LocalDate): Flow<List<MealPlan>> {
        val endDate = startDate.plusDays(6)
        return dao.getMealPlansForWeek(startDate.toString(), endDate.toString())
            .map { it.map { e -> e.toDomain() } }
    }

    override fun getMealPlansForDate(date: LocalDate): Flow<List<MealPlan>> =
        dao.getMealPlansForDate(date.toString()).map { it.map { e -> e.toDomain() } }

    override fun getRoutines(): Flow<List<MealPlan>> =
        dao.getRoutines().map { it.map { e -> e.toDomain() } }

    override suspend fun saveMealPlan(plan: MealPlan): Long =
        dao.insertMealPlan(MealPlanEntity.fromDomain(plan))

    override suspend fun updateMealPlan(plan: MealPlan) =
        dao.updateMealPlan(MealPlanEntity.fromDomain(plan))

    override suspend fun deleteMealPlan(id: Long) = dao.deleteMealPlan(id)
}
