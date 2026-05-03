package com.americanpizzabar.food.presentation.screen.shopping

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.americanpizzabar.food.domain.model.ShoppingItem

val SHOPPING_CATEGORIES = listOf("野菜", "肉・魚", "乳製品", "穀物・豆", "調味料", "飲み物", "お菓子", "その他")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    onNavigateUp: () -> Unit,
    viewModel: ShoppingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val displayItems = if (uiState.showChecked) uiState.items
    else uiState.items.filter { !it.isChecked }

    val grouped = displayItems.groupBy { it.category }

    if (uiState.showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteAllDialog,
            title = { Text("全て削除") },
            text = { Text("買い物リストの全アイテムを削除しますか？") },
            confirmButton = {
                TextButton(onClick = viewModel::deleteAllItems) { Text("削除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteAllDialog) { Text("キャンセル") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("買い物リスト")
                        val unchecked = uiState.items.count { !it.isChecked }
                        if (unchecked > 0) Text("残り ${unchecked}品", style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleShowChecked) {
                        Icon(
                            if (uiState.showChecked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "完了品の表示切替"
                        )
                    }
                    IconButton(onClick = viewModel::deleteCheckedItems) {
                        Icon(Icons.Default.PlaylistRemove, "完了品を削除")
                    }
                    IconButton(onClick = viewModel::showDeleteAllDialog) {
                        Icon(Icons.Default.DeleteSweep, "全て削除")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddItem) {
                Icon(Icons.Default.Add, "追加")
            }
        }
    ) { padding ->
        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("買い物リストは空です", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("レシピ提案から自動追加、または＋ボタンで手動追加できます",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                SHOPPING_CATEGORIES.forEach { category ->
                    val categoryItems = grouped[category] ?: return@forEach
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(categoryItems, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggle = { viewModel.toggleChecked(item.id) },
                            onDelete = { viewModel.deleteItem(item.id) }
                        )
                        HorizontalDivider()
                    }
                }
                // Items in uncategorized groups
                val unknownCategories = grouped.keys.filter { it !in SHOPPING_CATEGORIES }
                unknownCategories.forEach { category ->
                    val categoryItems = grouped[category] ?: return@forEach
                    item {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                            Text(category, style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                        }
                    }
                    items(categoryItems, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggle = { viewModel.toggleChecked(item.id) },
                            onDelete = { viewModel.deleteItem(item.id) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (uiState.isAddingItem) {
        AddShoppingItemSheet(
            uiState = uiState,
            onNameChange = viewModel::updateNewName,
            onAmountChange = viewModel::updateNewAmount,
            onUnitChange = viewModel::updateNewUnit,
            onCategoryChange = viewModel::updateNewCategory,
            onSave = viewModel::saveItem,
            onDismiss = viewModel::hideAddItem
        )
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val textColor by animateColorAsState(
        if (item.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.onSurface,
        label = "textColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (item.isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = item.isChecked, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    color = textColor
                )
            )
            if (item.amount.isNotBlank() || item.unit.isNotBlank()) {
                Text(
                    text = "${item.amount} ${item.unit}".trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
            }
            item.recipeName?.let { recipeName ->
                AssistChip(
                    onClick = {},
                    label = { Text(recipeName, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(24.dp)
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, "削除", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddShoppingItemSheet(
    uiState: ShoppingUiState,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("アイテムを追加", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.newItemName,
                onValueChange = onNameChange,
                label = { Text("食材名 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.newItemAmount,
                    onValueChange = onAmountChange,
                    label = { Text("数量") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.newItemUnit,
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
                SHOPPING_CATEGORIES.forEach { cat ->
                    FilterChip(
                        selected = uiState.newItemCategory == cat,
                        onClick = { onCategoryChange(cat) },
                        label = { Text(cat) }
                    )
                }
            }
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.newItemName.isNotBlank()
            ) {
                Text("追加する")
            }
        }
    }
}
