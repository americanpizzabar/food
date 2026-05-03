package com.americanpizzabar.food.domain.model

import java.time.LocalDate

data class PantryItem(
    val id: Long = 0,
    val name: String,
    val amount: String,
    val unit: String,
    val category: String,
    val expiryDate: LocalDate? = null,
    val notes: String = "",
    val imageUri: String? = null
)
