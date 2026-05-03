package com.americanpizzabar.food.presentation.screen.journal

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.americanpizzabar.food.domain.model.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("削除確認") },
            text = { Text("この日記を削除しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { viewModel.deleteEntry(it) }
                    showDeleteDialog = null
                }) { Text("削除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("キャンセル") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("食事日記", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, "戻る")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddEntry() }) {
                Icon(Icons.Default.Add, "追加")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("料理名で検索...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, "クリア")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(28.dp)
            )

            // Sort/filter options
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Sort chips
                item {
                    FilterChip(
                        selected = uiState.sortOrder == SortOrder.DATE_DESC,
                        onClick = { viewModel.updateSortOrder(SortOrder.DATE_DESC) },
                        label = { Text("新しい順") },
                        leadingIcon = if (uiState.sortOrder == SortOrder.DATE_DESC) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.sortOrder == SortOrder.DATE_ASC,
                        onClick = { viewModel.updateSortOrder(SortOrder.DATE_ASC) },
                        label = { Text("古い順") },
                        leadingIcon = if (uiState.sortOrder == SortOrder.DATE_ASC) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.sortOrder == SortOrder.RATING_DESC,
                        onClick = { viewModel.updateSortOrder(SortOrder.RATING_DESC) },
                        label = { Text("評価高い順") },
                        leadingIcon = if (uiState.sortOrder == SortOrder.RATING_DESC) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.sortOrder == SortOrder.RATING_ASC,
                        onClick = { viewModel.updateSortOrder(SortOrder.RATING_ASC) },
                        label = { Text("評価低い順") },
                        leadingIcon = if (uiState.sortOrder == SortOrder.RATING_ASC) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
                // Rating filter divider text
                item {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text("|", color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                // Rating filter chips
                item {
                    FilterChip(
                        selected = uiState.filterRating == null,
                        onClick = { viewModel.setFilterRating(null) },
                        label = { Text("全て") }
                    )
                }
                items((1..5).toList()) { star ->
                    FilterChip(
                        selected = uiState.filterRating == star,
                        onClick = {
                            viewModel.setFilterRating(if (uiState.filterRating == star) null else star)
                        },
                        label = { Text("${"★".repeat(star)}") }
                    )
                }
            }

            if (uiState.entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MenuBook,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "日記がありません",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "右下の＋ボタンで追加しましょう",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.entries, key = { it.id }) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            onClick = { onNavigateToDetail(entry.id) },
                            onDelete = { showDeleteDialog = entry.id }
                        )
                    }
                }
            }
        }

        if (uiState.isAddingEntry) {
            AddJournalEntrySheet(
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = { viewModel.hideAddEntry() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JournalEntryCard(
    entry: com.americanpizzabar.food.domain.model.JournalEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == EndToStart) {
                onDelete()
                false // we handle deletion via dialog, don't auto-dismiss
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == EndToStart)
                MaterialTheme.colorScheme.errorContainer else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onDelete() })
                },
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.recipeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    entry.mood?.let { mood ->
                        Text(
                            text = moodEmoji(mood),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StarRatingDisplay(rating = entry.rating, size = 14)
                }
                if (entry.comment.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = entry.comment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                entry.feelingAfter?.let { feeling ->
                    Spacer(Modifier.height(4.dp))
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                feelingAfterLabel(feeling),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddJournalEntrySheet(
    uiState: JournalUiState,
    viewModel: JournalViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "新しい日記を追加",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Recipe name
            OutlinedTextField(
                value = uiState.newRecipeName,
                onValueChange = { viewModel.updateNewRecipeName(it) },
                label = { Text("料理名 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Star rating
            Column {
                Text("評価", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                StarRatingSelector(
                    rating = uiState.newRating,
                    onRatingChange = { viewModel.updateNewRating(it) }
                )
            }

            // Comment
            OutlinedTextField(
                value = uiState.newComment,
                onValueChange = { viewModel.updateNewComment(it) },
                label = { Text("コメント") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            // Mood selector
            Column {
                Text("気分", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Mood.entries) { mood ->
                        FilterChip(
                            selected = uiState.newMood == mood,
                            onClick = {
                                viewModel.updateNewMood(if (uiState.newMood == mood) null else mood)
                            },
                            label = { Text("${moodEmoji(mood)} ${moodLabel(mood)}") }
                        )
                    }
                }
            }

            // Health condition
            Column {
                Text("体調", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HealthCondition.entries) { condition ->
                        FilterChip(
                            selected = uiState.newHealthCondition == condition,
                            onClick = {
                                viewModel.updateNewHealthCondition(
                                    if (uiState.newHealthCondition == condition) null else condition
                                )
                            },
                            label = { Text(healthConditionLabel(condition)) }
                        )
                    }
                }
            }

            // Feeling after
            Column {
                Text("食後の感覚", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FeelingAfter.entries) { feeling ->
                        FilterChip(
                            selected = uiState.newFeelingAfter == feeling,
                            onClick = {
                                viewModel.updateNewFeelingAfter(
                                    if (uiState.newFeelingAfter == feeling) null else feeling
                                )
                            },
                            label = { Text(feelingAfterLabel(feeling)) }
                        )
                    }
                }
            }

            // Save button
            Button(
                onClick = { viewModel.saveEntry() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.newRecipeName.isNotBlank()
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("保存する")
            }
        }
    }
}

@Composable
fun StarRatingDisplay(rating: Int, size: Int = 18) {
    Row {
        repeat(5) { index ->
            Text(
                text = if (index < rating) "★" else "☆",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = androidx.compose.ui.unit.TextUnit(size.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                color = if (index < rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun StarRatingSelector(rating: Int, onRatingChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(5) { index ->
            val star = index + 1
            IconButton(
                onClick = { onRatingChange(star) },
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = if (index < rating) "★" else "☆",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (index < rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

fun moodEmoji(mood: Mood): String = when (mood) {
    Mood.HAPPY -> "😊"
    Mood.NORMAL -> "😐"
    Mood.TIRED -> "😴"
    Mood.STRESSED -> "😤"
    Mood.ENERGETIC -> "⚡"
    Mood.RELAXED -> "😌"
    Mood.SAD -> "😢"
}

fun moodLabel(mood: Mood): String = when (mood) {
    Mood.HAPPY -> "幸せ"
    Mood.NORMAL -> "普通"
    Mood.TIRED -> "疲れ"
    Mood.STRESSED -> "ストレス"
    Mood.ENERGETIC -> "元気"
    Mood.RELAXED -> "リラックス"
    Mood.SAD -> "悲しい"
}

fun healthConditionLabel(condition: HealthCondition): String = when (condition) {
    HealthCondition.EXCELLENT -> "絶好調"
    HealthCondition.GOOD -> "良い"
    HealthCondition.FAIR -> "普通"
    HealthCondition.POOR -> "不調"
    HealthCondition.SICK -> "体調不良"
}

fun feelingAfterLabel(feeling: FeelingAfter): String = when (feeling) {
    FeelingAfter.VERY_GOOD -> "とても良い"
    FeelingAfter.GOOD -> "良い"
    FeelingAfter.NEUTRAL -> "普通"
    FeelingAfter.SLIGHTLY_UNWELL -> "少し不調"
    FeelingAfter.UNWELL -> "不調"
}
