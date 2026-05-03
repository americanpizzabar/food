package com.americanpizzabar.food.domain.usecase

import com.americanpizzabar.food.data.remote.ClaudeAiClient
import com.americanpizzabar.food.domain.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class ImportRecipeFromUrlUseCase @Inject constructor(
    private val client: ClaudeAiClient
) {
    suspend operator fun invoke(url: String): Result<Recipe> = runCatching {
        val htmlContent = withContext(Dispatchers.IO) {
            Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get()
                .body()
                .text()
        }
        client.importRecipeFromUrl(url, htmlContent).copy(
            sourceUrl = url,
            sourceName = extractDomain(url)
        )
    }

    private fun extractDomain(url: String): String {
        return try {
            val host = java.net.URL(url).host
            host.removePrefix("www.")
        } catch (e: Exception) {
            url
        }
    }
}
