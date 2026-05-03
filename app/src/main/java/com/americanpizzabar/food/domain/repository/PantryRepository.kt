package com.americanpizzabar.food.domain.repository

import com.americanpizzabar.food.domain.model.PantryItem
import kotlinx.coroutines.flow.Flow

interface PantryRepository {
    fun getAllItems(): Flow<List<PantryItem>>
    fun searchItems(query: String): Flow<List<PantryItem>>
    fun getExpiringItems(daysThreshold: Int): Flow<List<PantryItem>>
    suspend fun getItemById(id: Long): PantryItem?
    suspend fun saveItem(item: PantryItem): Long
    suspend fun updateItem(item: PantryItem)
    suspend fun deleteItem(id: Long)
}
