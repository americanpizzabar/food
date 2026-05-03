package com.americanpizzabar.food.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.americanpizzabar.food.BuildConfig
import com.americanpizzabar.food.data.remote.dto.*
import com.americanpizzabar.food.domain.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaudeAiClient @Inject constructor(
    private val apiService: ClaudeApiService,
    private val gson: Gson
) {
    private val model = BuildConfig.CLAUDE_MODEL

    suspend fun analyzeFoodPhoto(bitmap: Bitmap): Recipe {
        val base64 = bitmapToBase64(bitmap)
        val request = ClaudeRequest(
            model = model,
            system = SYSTEM_PROMPT_JSON,
            messages = listOf(
                ClaudeMessage(
                    role = "user",
                    content = listOf(
                        ClaudeContent.Image(
                            source = ImageSource(type = "base64", mediaType = "image/jpeg", data = base64)
                        ),
                        ClaudeContent.Text(text = PHOTO_ANALYSIS_PROMPT)
                    )
                )
            )
        )
        val response = apiService.sendMessage(request)
        return parseRecipeFromJson(response.content.firstOrNull()?.text ?: "{}")
    }

    suspend fun suggestRecipes(
        mood: String,
        craving: String,
        healthCondition: String,
        nutritionNeeds: String,
        pantryItems: List<String>,
        allergens: List<String>,
        dislikedIngredients: List<String>
    ): List<Recipe> {
        val prompt = buildSuggestionPrompt(
            mood, craving, healthCondition, nutritionNeeds,
            pantryItems, allergens, dislikedIngredients
        )
        val request = ClaudeRequest(
            model = model,
            system = SYSTEM_PROMPT_JSON,
            messages = listOf(
                ClaudeMessage(role = "user", content = listOf(ClaudeContent.Text(text = prompt)))
            )
        )
        val response = apiService.sendMessage(request)
        return parseRecipeListFromJson(response.content.firstOrNull()?.text ?: "[]")
    }

    suspend fun importRecipeFromUrl(url: String, htmlContent: String): Recipe {
        val prompt = """
            以下のWebページのHTMLからレシピを抽出してください。
            URL: $url
            HTML（先頭5000文字）: ${htmlContent.take(5000)}

            $PHOTO_ANALYSIS_PROMPT
        """.trimIndent()
        val request = ClaudeRequest(
            model = model,
            system = SYSTEM_PROMPT_JSON,
            messages = listOf(
                ClaudeMessage(role = "user", content = listOf(ClaudeContent.Text(text = prompt)))
            )
        )
        val response = apiService.sendMessage(request)
        return parseRecipeFromJson(response.content.firstOrNull()?.text ?: "{}")
    }

    suspend fun generatePlatingAdvice(recipe: Recipe): String {
        val prompt = """
            料理「${recipe.name}」について、以下の観点でアドバイスをしてください：
            1. おすすめの器の色・形
            2. 盛り付けのコツ
            3. 彩りを良くする飾り付け
            4. インスタ映えするアングル

            200文字以内で日本語で回答してください。
        """.trimIndent()
        val request = ClaudeRequest(
            model = model,
            messages = listOf(
                ClaudeMessage(role = "user", content = listOf(ClaudeContent.Text(text = prompt)))
            ),
            maxTokens = 512
        )
        val response = apiService.sendMessage(request)
        return response.content.firstOrNull()?.text ?: ""
    }

    suspend fun generateRemakeIdeas(recipe: Recipe): List<String> {
        val prompt = """
            「${recipe.name}」が余った場合、翌日以降のアレンジ・リメイク料理を3つ提案してください。
            JSON配列形式で返してください: ["アレンジ1", "アレンジ2", "アレンジ3"]
        """.trimIndent()
        val request = ClaudeRequest(
            model = model,
            system = SYSTEM_PROMPT_JSON,
            messages = listOf(
                ClaudeMessage(role = "user", content = listOf(ClaudeContent.Text(text = prompt)))
            ),
            maxTokens = 512
        )
        val response = apiService.sendMessage(request)
        return try {
            val text = response.content.firstOrNull()?.text ?: "[]"
            val jsonText = extractJsonFromText(text)
            gson.fromJson(jsonText, Array<String>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun generateShareCardContent(recipe: Recipe, comment: String, rating: Int): String {
        val prompt = """
            料理「${recipe.name}」について、SNS投稿用のキャプションを作ってください。
            評価: $rating/5, コメント: $comment
            カロリー: ${recipe.calories}kcal
            栄養: タンパク質${recipe.nutrition.protein}g, 炭水化物${recipe.nutrition.carbs}g, 脂質${recipe.nutrition.fat}g

            絵文字を使ったおしゃれな日本語キャプション（150文字以内）とハッシュタグ5個を返してください。
        """.trimIndent()
        val request = ClaudeRequest(
            model = model,
            messages = listOf(
                ClaudeMessage(role = "user", content = listOf(ClaudeContent.Text(text = prompt)))
            ),
            maxTokens = 512
        )
        val response = apiService.sendMessage(request)
        return response.content.firstOrNull()?.text ?: ""
    }

    suspend fun analyzeHealthCorrelation(logs: List<String>): String {
        val prompt = """
            以下の食事と体調のログを分析して、体調が良くなる食事のパターンを日本語で教えてください：
            ${logs.joinToString("\n")}

            200文字以内で分析結果とおすすめのメニューを提案してください。
        """.trimIndent()
        val request = ClaudeRequest(
            model = model,
            messages = listOf(
                ClaudeMessage(role = "user", content = listOf(ClaudeContent.Text(text = prompt)))
            ),
            maxTokens = 512
        )
        val response = apiService.sendMessage(request)
        return response.content.firstOrNull()?.text ?: ""
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun parseRecipeFromJson(text: String): Recipe {
        return try {
            val jsonText = extractJsonFromText(text)
            val json = gson.fromJson(jsonText, JsonObject::class.java)
            Recipe(
                name = json.get("name")?.asString ?: "不明な料理",
                description = json.get("description")?.asString ?: "",
                ingredients = parseIngredients(json),
                steps = parseSteps(json),
                calories = json.get("calories")?.asInt ?: 0,
                nutrition = parseNutrition(json),
                tags = parseTags(json),
                platingAdvice = json.get("platingAdvice")?.asString,
                remakeIdeas = parseRemakeIdeas(json),
                cookTimeMinutes = json.get("cookTimeMinutes")?.asInt ?: 30,
                prepTimeMinutes = json.get("prepTimeMinutes")?.asInt ?: 15,
                difficulty = parseDifficulty(json),
                servings = json.get("servings")?.asInt ?: 2
            )
        } catch (e: Exception) {
            Recipe(name = "解析エラー", description = text, ingredients = emptyList(), steps = emptyList(), calories = 0, nutrition = NutritionInfo(), tags = emptyList())
        }
    }

    private fun parseRecipeListFromJson(text: String): List<Recipe> {
        return try {
            val jsonText = extractJsonFromText(text)
            val jsonArray = gson.fromJson(jsonText, Array<JsonObject>::class.java)
            jsonArray.map { json ->
                Recipe(
                    name = json.get("name")?.asString ?: "不明",
                    description = json.get("description")?.asString ?: "",
                    ingredients = parseIngredients(json),
                    steps = parseSteps(json),
                    calories = json.get("calories")?.asInt ?: 0,
                    nutrition = parseNutrition(json),
                    tags = parseTags(json),
                    referenceImageUrl = json.get("imageSearchKeyword")?.asString,
                    cookTimeMinutes = json.get("cookTimeMinutes")?.asInt ?: 30,
                    prepTimeMinutes = json.get("prepTimeMinutes")?.asInt ?: 15,
                    difficulty = parseDifficulty(json),
                    servings = json.get("servings")?.asInt ?: 2
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractJsonFromText(text: String): String {
        val jsonStart = text.indexOf('{').takeIf { it >= 0 }
            ?: text.indexOf('[').takeIf { it >= 0 } ?: return text
        val isArray = text.indexOf('[').let { it >= 0 && (jsonStart < 0 || it < jsonStart) }
        val endChar = if (isArray) ']' else '}'
        val startChar = if (isArray) '[' else '{'
        val start = text.indexOf(startChar)
        val end = text.lastIndexOf(endChar) + 1
        return if (start >= 0 && end > start) text.substring(start, end) else text
    }

    private fun parseIngredients(json: JsonObject): List<Ingredient> {
        return try {
            val arr = json.getAsJsonArray("ingredients") ?: return emptyList()
            arr.mapNotNull { el ->
                val obj = el.asJsonObject
                Ingredient(
                    name = obj.get("name")?.asString ?: return@mapNotNull null,
                    amount = obj.get("amount")?.asString ?: "",
                    unit = obj.get("unit")?.asString ?: "",
                    calories = obj.get("calories")?.asInt ?: 0
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    private fun parseSteps(json: JsonObject): List<String> {
        return try {
            val arr = json.getAsJsonArray("steps") ?: return emptyList()
            arr.map { it.asString }
        } catch (e: Exception) { emptyList() }
    }

    private fun parseNutrition(json: JsonObject): NutritionInfo {
        return try {
            val n = json.getAsJsonObject("nutrition") ?: return NutritionInfo()
            NutritionInfo(
                protein = n.get("protein")?.asFloat ?: 0f,
                carbs = n.get("carbs")?.asFloat ?: 0f,
                fat = n.get("fat")?.asFloat ?: 0f,
                fiber = n.get("fiber")?.asFloat ?: 0f,
                sugar = n.get("sugar")?.asFloat ?: 0f,
                sodium = n.get("sodium")?.asFloat ?: 0f
            )
        } catch (e: Exception) { NutritionInfo() }
    }

    private fun parseTags(json: JsonObject): List<String> {
        return try {
            val arr = json.getAsJsonArray("tags") ?: return emptyList()
            arr.map { it.asString }
        } catch (e: Exception) { emptyList() }
    }

    private fun parseRemakeIdeas(json: JsonObject): List<String> {
        return try {
            val arr = json.getAsJsonArray("remakeIdeas") ?: return emptyList()
            arr.map { it.asString }
        } catch (e: Exception) { emptyList() }
    }

    private fun parseDifficulty(json: JsonObject): Difficulty {
        return try {
            val d = json.get("difficulty")?.asString ?: "MEDIUM"
            Difficulty.valueOf(d.uppercase())
        } catch (e: Exception) { Difficulty.MEDIUM }
    }

    companion object {
        private const val SYSTEM_PROMPT_JSON = """
あなたはプロの料理専門家AIです。必ず有効なJSONのみで回答してください。説明文は含めないでください。
"""

        private const val PHOTO_ANALYSIS_PROMPT = """
この料理の写真を分析し、以下のJSON形式で回答してください：
{
  "name": "料理名（日本語）",
  "description": "料理の説明（100文字以内）",
  "ingredients": [
    {"name": "食材名", "amount": "量", "unit": "単位", "calories": カロリー数値}
  ],
  "steps": ["手順1", "手順2", ...],
  "calories": 1人分カロリー数値,
  "nutrition": {
    "protein": タンパク質g,
    "carbs": 炭水化物g,
    "fat": 脂質g,
    "fiber": 食物繊維g,
    "sugar": 糖質g,
    "sodium": ナトリウムmg
  },
  "tags": ["タグ1", "タグ2"],
  "servings": 人数,
  "cookTimeMinutes": 調理時間,
  "prepTimeMinutes": 下準備時間,
  "difficulty": "EASY|MEDIUM|HARD",
  "platingAdvice": "盛り付けアドバイス",
  "remakeIdeas": ["リメイク案1", "リメイク案2"]
}"""
    }

    private fun buildSuggestionPrompt(
        mood: String, craving: String, healthCondition: String,
        nutritionNeeds: String, pantryItems: List<String>,
        allergens: List<String>, dislikedIngredients: List<String>
    ): String = """
気分: $mood
食べたいもの: $craving
体調: $healthCondition
必要栄養素: $nutritionNeeds
冷蔵庫の食材: ${pantryItems.joinToString(", ")}
アレルギー・除外食材: ${(allergens + dislikedIngredients).joinToString(", ")}

上記の条件に合うレシピを3つ提案してください。冷蔵庫の食材を優先的に使用し、アレルギー・除外食材は含めないでください。
以下のJSON配列形式で回答してください：
[
  {
    "name": "料理名",
    "description": "説明",
    "ingredients": [{"name": "食材", "amount": "量", "unit": "単位", "calories": 0}],
    "steps": ["手順1", "手順2"],
    "calories": 500,
    "nutrition": {"protein": 20, "carbs": 60, "fat": 15, "fiber": 5, "sugar": 10, "sodium": 800},
    "tags": ["タグ"],
    "servings": 2,
    "cookTimeMinutes": 30,
    "prepTimeMinutes": 10,
    "difficulty": "EASY",
    "imageSearchKeyword": "画像検索キーワード"
  }
]
"""
}
