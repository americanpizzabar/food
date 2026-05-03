package com.americanpizzabar.food.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.americanpizzabar.food.data.local.dao.*
import com.americanpizzabar.food.data.local.entity.*

@Database(
    entities = [
        RecipeEntity::class,
        JournalEntity::class,
        ShoppingItemEntity::class,
        PantryItemEntity::class,
        MealPlanEntity::class,
        HealthLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun journalDao(): JournalDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun pantryDao(): PantryDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun healthLogDao(): HealthLogDao
}
