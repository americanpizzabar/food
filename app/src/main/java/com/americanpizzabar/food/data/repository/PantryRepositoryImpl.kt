package com.americanpizzabar.food.data.repository

import com.americanpizzabar.food.data.local.dao.PantryDao
import com.americanpizzabar.food.data.local.entity.PantryItemEntity
import com.americanpizzabar.food.domain.model.PantryItem
import com.americanpizzabar.food.domain.repository.PantryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class PantryRepositoryImpl @Inject constructor(
    private val dao: PantryDao
) : PantryRepository {
    override fun getAllItems(): Flow<List<PantryItem>> =
        dao.getAllItems().map { it.map { e -> e.toDomain() } }

    override fun searchItems(query: String): Flow<List<PantryItem>> =
        dao.searchItems(query).map { it.map { e -> e.toDomain() } }

    override fun getExpiringItems(daysThreshold: Int): Flow<List<PantryItem>> {
        val threshold = LocalDate.now().plusDays(daysThreshold.toLong()).toString()
        return dao.getExpiringItems(threshold).map { it.map { e -> e.toDomain() } }
    }

    override suspend fun getItemById(id: Long): PantryItem? =
        dao.getItemById(id)?.toDomain()

    override suspend fun saveItem(item: PantryItem): Long =
        dao.insertItem(PantryItemEntity.fromDomain(item))

    override suspend fun updateItem(item: PantryItem) =
        dao.updateItem(PantryItemEntity.fromDomain(item))

    override suspend fun deleteItem(id: Long) = dao.deleteItem(id)
}
