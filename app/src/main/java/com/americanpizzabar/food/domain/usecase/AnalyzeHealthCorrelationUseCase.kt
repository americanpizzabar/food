package com.americanpizzabar.food.domain.usecase

import com.americanpizzabar.food.data.remote.ClaudeAiClient
import com.americanpizzabar.food.domain.repository.HealthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AnalyzeHealthCorrelationUseCase @Inject constructor(
    private val client: ClaudeAiClient,
    private val healthRepository: HealthRepository
) {
    suspend operator fun invoke(): Result<String> = runCatching {
        val logs = healthRepository.getAllLogs().first()
        if (logs.isEmpty()) return@runCatching "まだデータが不足しています。食事の記録を続けてください。"

        val logSummaries = logs.take(30).map { log ->
            "${log.date}: ${log.recipeName} → 食後体調: ${log.feelingAfter}/5, エネルギー: ${log.energyLevel}/5"
        }
        client.analyzeHealthCorrelation(logSummaries)
    }
}
