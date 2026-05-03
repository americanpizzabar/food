package com.americanpizzabar.food.domain.model

data class ShoppingItem(
    val id: Long = 0,
    val name: String,
    val amount: String,
    val unit: String,
    val category: String,
    val isChecked: Boolean = false,
    val recipeId: Long? = null,
    val recipeName: String? = null,
    val notes: String = "",
    val sortOrder: Int = 0
)
