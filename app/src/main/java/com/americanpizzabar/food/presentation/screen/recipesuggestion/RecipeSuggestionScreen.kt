package com.americanpizzabar.food.presentation.screen.recipesuggestion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.americanpizzabar.food.domain.model.Recipe

// ---------------------------------------------------------------------------
// Mood options
// ---------------------------------------------------------------------------

private data class MoodOption(val emoji: String, val label: String, val value: String)

private val moodOptions = listOf(
    MoodOption("😊", "嬉しい", "嬉しい"),
    MoodOption("😴", "疲れた", "疲れた"),
    MoodOption("😰", "ストレス", "ストレス"),
    MoodOption("⚡", "元気", "元気"),
    MoodOption("😌", "リラックス", "リラックス")
)

private val healthConditionOptions = listOf("絶好調", "良好", "普通", "不調", "体調不良")

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSuggestionScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecipeSuggestionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "閉じる",
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.shoppingListGenerated) {
        if (uiState.shoppingListGenerated) {
            snackbarHostState.showSnackbar("買い物リストに追加しました")
        }
    }

    LaunchedEffect(uiState.savedRecipeId) {
        if (uiState.savedRecipeId != null) {
            snackbarHostState.showSnackbar("レシピを保存しました")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("レシピ提案") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "戻る")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input form
            item {
                InputFormCard(
                    selectedMood = uiState.mood,
                    onMoodSelect = viewModel::updateMood,
                    craving = uiState.craving,
                    onCravingChange = viewModel::updateCraving,
                    selectedHealthCondition = uiState.healthCondition,
                    onHealthConditionSelect = viewModel::updateHealthCondition,
                    nutritionNeeds = uiState.nutritionNeeds,
                    onNutritionNeedsChange = viewModel::updateNutritionNeeds
                )
            }

            // Submit button
            item {
                Button(
                    onClick = viewModel::getSuggestions,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("提案中...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("提案する", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Results
            if (uiState.suggestions.isNotEmpty()) {
                item {
                    Text(
                        "おすすめレシピ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(uiState.suggestions) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        isSaved = uiState.savedRecipeId != null,
                        onSave = { viewModel.saveRecipe(recipe) },
                        onAddToShoppingList = { viewModel.addToShoppingList(recipe) }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Input form card
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputFormCard(
    selectedMood: String,
    onMoodSelect: (String) -> Unit,
    craving: String,
    onCravingChange: (String) -> Unit,
    selectedHealthCondition: String,
    onHealthConditionSelect: (String) -> Unit,
    nutritionNeeds: String,
    onNutritionNeedsChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "今の状態を教えてください",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Mood selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("気分", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(moodOptions) { option ->
                        val selected = selectedMood == option.value
                        FilterChip(
                            selected = selected,
                            onClick = { onMoodSelect(option.value) },
                            label = {
                                Text("${option.emoji} ${option.label}")
                            },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Craving text field
            OutlinedTextField(
                value = craving,
                onValueChange = onCravingChange,
                label = { Text("何が食べたい？") },
                placeholder = { Text("例: さっぱりしたもの、温かいスープ...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )

            // Health condition selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("体調", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(healthConditionOptions) { condition ->
                        val selected = selectedHealthCondition == condition
                        FilterChip(
                            selected = selected,
                            onClick = { onHealthConditionSelect(condition) },
                            label = { Text(condition) },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Nutrition needs text field
            OutlinedTextField(
                value = nutritionNeeds,
                onValueChange = onNutritionNeedsChange,
                label = { Text("必要な栄養素") },
                placeholder = { Text("例: タンパク質多め、鉄分、ビタミンC...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.FitnessCenter, null) }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Recipe result card
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeCard(
    recipe: Recipe,
    isSaved: Boolean,
    onSave: () -> Unit,
    onAddToShoppingList: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: name + difficulty badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                recipe.difficulty?.let { difficulty ->
                    DifficultyBadge(difficulty = difficulty)
                }
            }

            Spacer(Modifier.height(4.dp))

            // Description
            Text(
                recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Meta info row: calories, ingredient count, cook time
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaChip(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "${recipe.calories} kcal"
                )
                MetaChip(
                    icon = Icons.Default.Kitchen,
                    label = "${recipe.ingredients.size}品"
                )
                recipe.cookTimeMinutes?.let { minutes ->
                    MetaChip(
                        icon = Icons.Default.Timer,
                        label = "${minutes}分"
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaved
                ) {
                    Icon(
                        if (isSaved) Icons.Default.Check else Icons.Default.BookmarkAdd,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isSaved) "保存済み" else "保存", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onAddToShoppingList,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("買い物リストへ", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Expandable toggle
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (expanded) "詳細を隠す" else "材料・作り方を見る")
                Spacer(Modifier.width(4.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider()

                    // Ingredients
                    if (recipe.ingredients.isNotEmpty()) {
                        Text(
                            "材料",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        recipe.ingredients.forEach { ingredient ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    ingredient.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    ingredient.amount,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Steps
                    if (recipe.steps.isNotEmpty()) {
                        Text(
                            "作り方",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        recipe.steps.forEachIndexed { index, step ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Text(step, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Small helper composables
// ---------------------------------------------------------------------------

@Composable
private fun DifficultyBadge(difficulty: String) {
    val containerColor = when (difficulty.lowercase()) {
        "easy", "簡単" -> MaterialTheme.colorScheme.primaryContainer
        "medium", "普通" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (difficulty.lowercase()) {
        "easy", "簡単" -> MaterialTheme.colorScheme.onPrimaryContainer
        "medium", "普通" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor
    ) {
        Text(
            difficulty,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
