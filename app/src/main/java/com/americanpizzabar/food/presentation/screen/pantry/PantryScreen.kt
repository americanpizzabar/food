package com.americanpizzabar.food.presentation.screen.pantry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.americanpizzabar.food.domain.model.PantryItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

val PANTRY_CATEGORIES = listOf("野菜", "肉・魚", "乳製品", "穀物・豆", "調味料", "飲み物", "缶詰・乾物", "冷凍食品", "その他")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    onNavigateUp: () -> Unit,
    viewModel: PantryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val grouped = uiState.items.groupBy { it.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("パントリー")
                        Text("${uiState.items.size}品", style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddItem) {
                Icon(Icons.Default.Add, "追加")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearch,
                placeholder = { Text("食材を検索") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            // Expiring items warning
            if (uiState.expiringItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("もうすぐ期限切れ", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                        uiState.expiringItems.forEach { item ->
                            val days = item.expiryDate?.let {
                                ChronoUnit.DAYS.between(LocalDate.now(), it)
                            } ?: 0
                            val label = if (days <= 0) "今日が期限" else "${days}日後"
                            Text("• ${item.name}（$label）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            if (uiState.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Kitchen, null, modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Text("パントリーは空です", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("＋ボタンで食材を登録してください",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    PANTRY_CATEGORIES.forEach { category ->
                        val catItems = grouped[category] ?: return@forEach
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "$category (${catItems.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        items(catItems, key = { it.id }) { item ->
                            PantryItemRow(
                                item = item,
                                onEdit = { viewModel.startEditItem(item) },
                                onDelete = { viewModel.deleteItem(item.id) }
                            )
                            HorizontalDivider()
                        }
                    }
                    // Unknown categories
                    val unknownCats = grouped.keys.filter { it !in PANTRY_CATEGORIES }
                    unknownCats.forEach { category ->
                        val catItems = grouped[category] ?: return@forEach
                        item {
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                                Text(category, style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                            }
                        }
                        items(catItems, key = { it.id }) { item ->
                            PantryItemRow(
                                item = item,
                                onEdit = { viewModel.startEditItem(item) },
                                onDelete = { viewModel.deleteItem(item.id) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    if (uiState.isAddingItem) {
        PantryItemFormSheet(
            uiState = uiState,
            isEditing = uiState.editingItem != null,
            onNameChange = viewModel::updateFormName,
            onAmountChange = viewModel::updateFormAmount,
            onUnitChange = viewModel::updateFormUnit,
            onCategoryChange = viewModel::updateFormCategory,
            onExpiryChange = viewModel::updateFormExpiry,
            onNotesChange = viewModel::updateFormNotes,
            onSave = viewModel::saveItem,
            onDismiss = viewModel::hideForm
        )
    }
}

@Composable
private fun PantryItemRow(
    item: PantryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isExpiring = item.expiryDate?.let {
        ChronoUnit.DAYS.between(LocalDate.now(), it) <= 3
    } ?: false

    ListItem(
        headlineContent = {
            Text(
                item.name,
                color = if (isExpiring) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Column {
                if (item.amount.isNotBlank()) Text("${item.amount} ${item.unit}".trim(),
                    style = MaterialTheme.typography.bodySmall)
                item.expiryDate?.let { expiry ->
                    val days = ChronoUnit.DAYS.between(LocalDate.now(), expiry)
                    val expiryText = when {
                        days < 0 -> "期限切れ"
                        days == 0L -> "今日が期限"
                        else -> "期限: ${expiry.format(DateTimeFormatter.ofPattern("M/d"))} (${days}日後)"
                    }
                    Text(
                        expiryText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (days <= 3) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.notes.isNotBlank()) Text(item.notes, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "編集", modifier = Modifier.size(20.dp)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "削除", modifier = Modifier.size(20.dp)) }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantryItemFormSheet(
    uiState: PantryUiState,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onExpiryChange: (LocalDate?) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (isEditing) "食材を編集" else "食材を追加",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = uiState.formName,
                onValueChange = onNameChange,
                label = { Text("食材名 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.formAmount,
                    onValueChange = onAmountChange,
                    label = { Text("数量") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.formUnit,
                    onValueChange = onUnitChange,
                    label = { Text("単位") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("g, ml, 個") }
                )
            }
            Text("カテゴリ", style = MaterialTheme.typography.labelMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PANTRY_CATEGORIES.forEach { cat ->
                    FilterChip(
                        selected = uiState.formCategory == cat,
                        onClick = { onCategoryChange(cat) },
                        label = { Text(cat) }
                    )
                }
            }
            // Expiry date
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(uiState.formExpiry?.format(DateTimeFormatter.ofPattern("yyyy/M/d"))
                    ?: "賞味期限を設定（任意）")
            }
            if (uiState.formExpiry != null) {
                TextButton(onClick = { onExpiryChange(null) }) { Text("期限をクリア") }
            }
            OutlinedTextField(
                value = uiState.formNotes,
                onValueChange = onNotesChange,
                label = { Text("メモ") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.formName.isNotBlank()
            ) {
                Text(if (isEditing) "更新する" else "追加する")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onExpiryChange(
                            java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                        )
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("キャンセル") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
