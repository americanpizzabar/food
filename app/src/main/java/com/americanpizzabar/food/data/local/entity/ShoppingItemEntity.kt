package com.americanpizzabar.food.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.americanpizzabar.food.domain.model.ShoppingItem

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: String,
    val unit: String,
    val category: String,
    val isChecked: Boolean = false,
    val recipeId: Long? = null,
    val recipeName: String? = null,
    val notes: String = "",
    val sortOrder: Int = 0
) {
    fun toDomain() = ShoppingItem(
        id = id, name = name, amount = amount, unit = unit,
        category = category, isChecked = isChecked, recipeId = recipeId,
        recipeName = recipeName, notes = notes, sortOrder = sortOrder
    )

    companion object {
        fun fromDomain(item: ShoppingItem) = ShoppingItemEntity(
            id = item.id, name = item.name, amount = item.amount, unit = item.unit,
            category = item.category, isChecked = item.isChecked, recipeId = item.recipeId,
            recipeName = item.recipeName, notes = item.notes, sortOrder = item.sortOrder
        )
    }
}
