package com.americanpizzabar.food.domain.repository

import com.americanpizzabar.food.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun getAllItems(): Flow<List<ShoppingItem>>
    fun getUncheckedItems(): Flow<List<ShoppingItem>>
    suspend fun saveItem(item: ShoppingItem): Long
    suspend fun saveItems(items: List<ShoppingItem>)
    suspend fun updateItem(item: ShoppingItem)
    suspend fun toggleChecked(id: Long)
    suspend fun deleteItem(id: Long)
    suspend fun deleteCheckedItems()
    suspend fun deleteAllItems()
}
