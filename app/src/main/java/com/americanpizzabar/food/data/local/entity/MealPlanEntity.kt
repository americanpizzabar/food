package com.americanpizzabar.food.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.americanpizzabar.food.domain.model.MealPlan
import com.americanpizzabar.food.domain.model.MealType
import java.time.LocalDate

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val mealType: String,
    val recipeId: Long? = null,
    val recipeName: String,
    val notes: String = "",
    val isRoutine: Boolean = false,
    val routineTag: String? = null
) {
    fun toDomain() = MealPlan(
        id = id, date = LocalDate.parse(date), mealType = MealType.valueOf(mealType),
        recipeId = recipeId, recipeName = recipeName, notes = notes,
        isRoutine = isRoutine, routineTag = routineTag
    )

    companion object {
        fun fromDomain(plan: MealPlan) = MealPlanEntity(
            id = plan.id, date = plan.date.toString(), mealType = plan.mealType.name,
            recipeId = plan.recipeId, recipeName = plan.recipeName, notes = plan.notes,
            isRoutine = plan.isRoutine, routineTag = plan.routineTag
        )
    }
}
