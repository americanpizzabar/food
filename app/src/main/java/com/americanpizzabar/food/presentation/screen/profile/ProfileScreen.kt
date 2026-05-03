package com.americanpizzabar.food.presentation.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val DIETARY_RESTRICTIONS = listOf(
    "ベジタリアン", "ヴィーガン", "グルテンフリー", "乳製品不使用", "卵不使用", "低糖質"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateUp: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedMessage) {
        uiState.savedMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSavedMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("プロフィール設定") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Claude API Key section
            item {
                SectionCard(title = "Claude APIキー", icon = Icons.Default.Key) {
                    Text(
                        "AIによる写真解析・レシピ提案機能を使うにはAPIキーが必要です。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.prefs.claudeApiKey,
                        onValueChange = viewModel::updateApiKey,
                        label = { Text("APIキー") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (uiState.apiKeyVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = viewModel::toggleApiKeyVisible) {
                                Icon(
                                    if (uiState.apiKeyVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null
                                )
                            }
                        },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::saveApiKey, modifier = Modifier.fillMaxWidth()) {
                        Text("APIキーを保存")
                    }
                }
            }

            // Allergens section
            item {
                SectionCard(title = "アレルギー食材", icon = Icons.Default.Warning) {
                    Text(
                        "登録した食材を含むレシピは自動的にフィルタリングされます",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.prefs.allergens.forEach { allergen ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = { Text(allergen) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { viewModel.removeAllergen(allergen) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(Icons.Default.Close, "削除", modifier = Modifier.size(14.dp))
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.newAllergen,
                            onValueChange = viewModel::updateNewAllergen,
                            placeholder = { Text("例: 卵, 小麦") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.addAllergen(uiState.newAllergen) }
                            )
                        )
                        FilledTonalButton(onClick = { viewModel.addAllergen(uiState.newAllergen) }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }
            }

            // Disliked ingredients section
            item {
                SectionCard(title = "苦手食材", icon = Icons.Default.ThumbDown) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.prefs.dislikedIngredients.forEach { item ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = { Text(item) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { viewModel.removeDisliked(item) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(Icons.Default.Close, "削除", modifier = Modifier.size(14.dp))
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.newDisliked,
                            onValueChange = viewModel::updateNewDisliked,
                            placeholder = { Text("例: パクチー, レバー") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.addDisliked(uiState.newDisliked) }
                            )
                        )
                        FilledTonalButton(onClick = { viewModel.addDisliked(uiState.newDisliked) }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }
            }

            // Dietary restrictions section
            item {
                SectionCard(title = "食事制限", icon = Icons.Default.NoFood) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DIETARY_RESTRICTIONS.forEach { restriction ->
                            FilterChip(
                                selected = restriction in uiState.prefs.dietaryRestrictions,
                                onClick = { viewModel.toggleDietaryRestriction(restriction) },
                                label = { Text(restriction) }
                            )
                        }
                    }
                }
            }

            // Serving size
            item {
                SectionCard(title = "デフォルト人数", icon = Icons.Default.People) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (uiState.prefs.servingSize > 1)
                                    viewModel.updateServingSize(uiState.prefs.servingSize - 1)
                            }
                        ) { Icon(Icons.Default.Remove, null) }
                        Text(
                            "${uiState.prefs.servingSize}人分",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.updateServingSize(uiState.prefs.servingSize + 1) }
                        ) { Icon(Icons.Default.Add, null) }
                    }
                }
            }

            // Preferences toggles
            item {
                SectionCard(title = "アプリ設定", icon = Icons.Default.Settings) {
                    ListItem(
                        headlineContent = { Text("ダークモード") },
                        trailingContent = {
                            Switch(
                                checked = uiState.prefs.darkMode,
                                onCheckedChange = { viewModel.toggleDarkMode() }
                            )
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("通知") },
                        supportingContent = { Text("献立リマインダーなど") },
                        trailingContent = {
                            Switch(
                                checked = uiState.prefs.notificationsEnabled,
                                onCheckedChange = { viewModel.toggleNotifications() }
                            )
                        }
                    )
                }
            }

            // Health correlation analysis
            item {
                SectionCard(title = "体調分析", icon = Icons.Default.Favorite) {
                    Text(
                        "食事と体調のログを分析して、あなたに最適なメニューを提案します。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = viewModel::runHealthAnalysis,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isAnalyzingHealth
                    ) {
                        if (uiState.isAnalyzingHealth) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("体調分析を実行")
                    }
                    if (uiState.healthInsight.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Card(colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )) {
                            Text(
                                uiState.healthInsight,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // App info
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("FoodAI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Powered by Claude claude-sonnet-4-6",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
