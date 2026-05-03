package com.americanpizzabar.food.domain.usecase

import android.graphics.Bitmap
import com.americanpizzabar.food.data.remote.ClaudeAiClient
import com.americanpizzabar.food.domain.model.Recipe
import javax.inject.Inject

class AnalyzeFoodPhotoUseCase @Inject constructor(
    private val client: ClaudeAiClient
) {
    suspend operator fun invoke(bitmap: Bitmap): Result<Recipe> = runCatching {
        client.analyzeFoodPhoto(bitmap)
    }
}
