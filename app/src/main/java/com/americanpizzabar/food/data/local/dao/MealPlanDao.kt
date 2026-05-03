package com.americanpizzabar.food.data.local.dao

import androidx.room.*
import com.americanpizzabar.food.data.local.entity.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans WHERE date >= :startDate AND date <= :endDate ORDER BY date, mealType")
    fun getMealPlansForWeek(startDate: String, endDate: String): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE date = :date ORDER BY mealType")
    fun getMealPlansForDate(date: String): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE isRoutine = 1")
    fun getRoutines(): Flow<List<MealPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(plan: MealPlanEntity): Long

    @Update
    suspend fun updateMealPlan(plan: MealPlanEntity)

    @Query("DELETE FROM meal_plans WHERE id = :id")
    suspend fun deleteMealPlan(id: Long)
}
