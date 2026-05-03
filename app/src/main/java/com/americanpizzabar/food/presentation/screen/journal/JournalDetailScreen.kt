package com.americanpizzabar.food.presentation.screen.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.americanpizzabar.food.domain.model.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalDetailScreen(
    entryId: Long,
    onNavigateUp: () -> Unit,
    viewModel: JournalDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onNavigateUp()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("削除確認") },
            text = { Text("この日記を削除しますか？この操作は元に戻せません。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteEntry()
                }) { Text("削除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("キャンセル") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditing) "編集中" else "日記詳細",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isEditing) viewModel.exitEditMode() else onNavigateUp()
                    }) {
                        Icon(
                            if (uiState.isEditing) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(
                            onClick = { viewModel.saveChanges() },
                            enabled = uiState.editRecipeName.isNotBlank()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "保存")
                        }
                    } else {
                        IconButton(onClick = { viewModel.enterEditMode() }) {
                            Icon(Icons.Default.Edit, contentDescription = "編集")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "削除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            uiState.error ?: "エラーが発生しました",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            else -> {
                val entry = uiState.entry
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.isEditing) {
                        EditModeContent(uiState = uiState, viewModel = viewModel)
                    } else if (entry != null) {
                        ViewModeContent(entry = entry)
                    }

                    if (uiState.isSaved) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text("保存しました", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    if (!uiState.isEditing && uiState.showHealthLogForm && entry != null) {
                        HealthLogFormCard(uiState = uiState, viewModel = viewModel)
                    }

                    if (uiState.isHealthLogSaved) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    "健康ログを保存しました",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewModeContent(entry: JournalEntry) {
    Text(
        text = entry.recipeName,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = entry.date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        StarRatingDisplay(rating = entry.rating, size = 20)
    }

    HorizontalDivider()

    entry.mood?.let { mood ->
        DetailRow(label = "気分", value = "${moodEmoji(mood)} ${moodLabel(mood)}")
    }

    entry.healthCondition?.let { condition ->
        DetailRow(label = "体調", value = healthConditionLabel(condition))
    }

    entry.feelingAfter?.let { feeling ->
        DetailRow(label = "食後の感覚", value = feelingAfterLabel(feeling))
    }

    if (entry.comment.isNotBlank()) {
        HorizontalDivider()
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "コメント",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = entry.comment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditModeContent(
    uiState: JournalDetailUiState,
    viewModel: JournalDetailViewModel
) {
    Text(
        text = "日記を編集",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    OutlinedTextField(
        value = uiState.editRecipeName,
        onValueChange = { viewModel.updateEditRecipeName(it) },
        label = { Text("料理名 *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = uiState.editRecipeName.isBlank()
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("評価", style = MaterialTheme.typography.labelLarge)
        StarRatingSelector(
            rating = uiState.editRating,
            onRatingChange = { viewModel.updateEditRating(it) }
        )
    }

    OutlinedTextField(
        value = uiState.editComment,
        onValueChange = { viewModel.updateEditComment(it) },
        label = { Text("コメント") },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        maxLines = 5
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("気分", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Mood.entries.forEach { mood ->
                FilterChip(
                    selected = uiState.editMood == mood,
                    onClick = { viewModel.updateEditMood(if (uiState.editMood == mood) null else mood) },
                    label = { Text("${moodEmoji(mood)} ${moodLabel(mood)}") }
                )
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("体調", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HealthCondition.entries.forEach { condition ->
                FilterChip(
                    selected = uiState.editHealthCondition == condition,
                    onClick = {
                        viewModel.updateEditHealthCondition(
                            if (uiState.editHealthCondition == condition) null else condition
                        )
                    },
                    label = { Text(healthConditionLabel(condition)) }
                )
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("食後の感覚", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FeelingAfter.entries.forEach { feeling ->
                FilterChip(
                    selected = uiState.editFeelingAfter == feeling,
                    onClick = {
                        viewModel.updateEditFeelingAfter(
                            if (uiState.editFeelingAfter == feeling) null else feeling
                        )
                    },
                    label = { Text(feelingAfterLabel(feeling)) }
                )
            }
        }
    }

    Button(
        onClick = { viewModel.saveChanges() },
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.editRecipeName.isNotBlank()
    ) {
        Icon(Icons.Default.Save, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("変更を保存する")
    }
}

@Composable
private fun HealthLogFormCard(
    uiState: JournalDetailUiState,
    viewModel: JournalDetailViewModel
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "健康ログを記録",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "この食事の健康への影響を記録しましょう",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HealthSlider(
                label = "食前の体調",
                value = uiState.healthLogFeelingBefore,
                onValueChange = { viewModel.updateHealthLogFeelingBefore(it) }
            )
            HealthSlider(
                label = "食後の体調",
                value = uiState.healthLogFeelingAfter,
                onValueChange = { viewModel.updateHealthLogFeelingAfter(it) }
            )
            HealthSlider(
                label = "エネルギーレベル",
                value = uiState.healthLogEnergyLevel,
                onValueChange = { viewModel.updateHealthLogEnergyLevel(it) }
            )
            HealthSlider(
                label = "消化の快適さ",
                value = uiState.healthLogDigestiveComfort,
                onValueChange = { viewModel.updateHealthLogDigestiveComfort(it) }
            )

            OutlinedTextField(
                value = uiState.healthLogNotes,
                onValueChange = { viewModel.updateHealthLogNotes(it) },
                label = { Text("メモ") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                maxLines = 3
            )

            Button(
                onClick = { viewModel.saveHealthLog() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("健康ログを保存する")
            }
        }
    }
}

@Composable
private fun HealthSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                "$value / 5",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt().coerceIn(1, 5)) },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
