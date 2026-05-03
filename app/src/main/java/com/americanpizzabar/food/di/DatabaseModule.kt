package com.americanpizzabar.food.di

import android.content.Context
import androidx.room.Room
import com.americanpizzabar.food.data.local.AppDatabase
import com.americanpizzabar.food.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "foodai.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideRecipeDao(db: AppDatabase): RecipeDao = db.recipeDao()
    @Provides fun provideJournalDao(db: AppDatabase): JournalDao = db.journalDao()
    @Provides fun provideShoppingDao(db: AppDatabase): ShoppingDao = db.shoppingDao()
    @Provides fun providePantryDao(db: AppDatabase): PantryDao = db.pantryDao()
    @Provides fun provideMealPlanDao(db: AppDatabase): MealPlanDao = db.mealPlanDao()
    @Provides fun provideHealthLogDao(db: AppDatabase): HealthLogDao = db.healthLogDao()
}
