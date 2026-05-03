package com.americanpizzabar.food.data.repository

import com.americanpizzabar.food.data.local.dao.ShoppingDao
import com.americanpizzabar.food.data.local.entity.ShoppingItemEntity
import com.americanpizzabar.food.domain.model.ShoppingItem
import com.americanpizzabar.food.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShoppingRepositoryImpl @Inject constructor(
    private val dao: ShoppingDao
) : ShoppingRepository {
    override fun getAllItems(): Flow<List<ShoppingItem>> =
        dao.getAllItems().map { it.map { e -> e.toDomain() } }

    override fun getUncheckedItems(): Flow<List<ShoppingItem>> =
        dao.getUncheckedItems().map { it.map { e -> e.toDomain() } }

    override suspend fun saveItem(item: ShoppingItem): Long =
        dao.insertItem(ShoppingItemEntity.fromDomain(item))

    override suspend fun saveItems(items: List<ShoppingItem>) =
        dao.insertItems(items.map { ShoppingItemEntity.fromDomain(it) })

    override suspend fun updateItem(item: ShoppingItem) =
        dao.updateItem(ShoppingItemEntity.fromDomain(item))

    override suspend fun toggleChecked(id: Long) = dao.toggleChecked(id)
    override suspend fun deleteItem(id: Long) = dao.deleteItem(id)
    override suspend fun deleteCheckedItems() = dao.deleteCheckedItems()
    override suspend fun deleteAllItems() = dao.deleteAllItems()
}
