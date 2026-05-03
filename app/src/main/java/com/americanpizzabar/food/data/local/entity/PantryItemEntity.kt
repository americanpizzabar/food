package com.americanpizzabar.food.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.americanpizzabar.food.domain.model.PantryItem
import java.time.LocalDate

@Entity(tableName = "pantry_items")
data class PantryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: String,
    val unit: String,
    val category: String,
    val expiryDate: String? = null,
    val notes: String = "",
    val imageUri: String? = null
) {
    fun toDomain() = PantryItem(
        id = id, name = name, amount = amount, unit = unit, category = category,
        expiryDate = expiryDate?.let { LocalDate.parse(it) },
        notes = notes, imageUri = imageUri
    )

    companion object {
        fun fromDomain(item: PantryItem) = PantryItemEntity(
            id = item.id, name = item.name, amount = item.amount, unit = item.unit,
            category = item.category, expiryDate = item.expiryDate?.toString(),
            notes = item.notes, imageUri = item.imageUri
        )
    }
}
