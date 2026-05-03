package com.americanpizzabar.food.presentation.screen.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.americanpizzabar.food.domain.model.MealPlan
import com.americanpizzabar.food.domain.model.MealType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DAY_LABELS = listOf("月", "火", "水", "木", "金", "土", "日")
private val MEAL_TYPES = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)
private val MEAL_LABELS = mapOf(
    MealType.BREAKFAST to "朝食",
    MealType.LUNCH to "昼食",
    MealType.DINNER to "夕食",
    MealType.SNACK to "間食"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateUp: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val weekDays = (0..6).map { uiState.weekStart.plusDays(it.toLong()) }
    val weekLabel = run {
        val start = uiState.weekStart.format(DateTimeFormatter.ofPattern("M/d"))
        val end = uiState.weekStart.plusDays(6).format(DateTimeFormatter.ofPattern("M/d"))
        "$start〜$end"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("献立カレンダー") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = viewModel::goToToday) {
                        Icon(Icons.Default.Today, "今日")
                    }
                    IconButton(onClick = viewModel::showRoutineDialog) {
                        Icon(Icons.Default.AutoAwesome, "ルーティン")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Week navigation
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = viewModel::previousWeek) {
                    Icon(Icons.Default.ChevronLeft, "前の週")
                }
                Text(
                    weekLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = viewModel::nextWeek) {
                    Icon(Icons.Default.ChevronRight, "次の週")
                }
            }

            // Calendar grid header
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Spacer(modifier = Modifier.width(48.dp)) // meal type label width
                weekDays.forEachIndexed { index, date ->
                    val isToday = date == today
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            DAY_LABELS[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = when (index) {
                                5 -> MaterialTheme.colorScheme.primary
                                6 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        val bgColor = if (isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = bgColor,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isToday) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Calendar grid body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                MEAL_TYPES.forEach { mealType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        // Meal type label
                        Box(
                            modifier = Modifier.width(48.dp).height(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                MEAL_LABELS[mealType] ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Day cells
                        weekDays.forEach { date ->
                            val plansForSlot = uiState.mealPlans.filter {
                                it.date == date && it.mealType == mealType
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp)
                                    .padding(2.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.showAddDialog(date, mealType) }
                                    .padding(4.dp)
                            ) {
                                if (plansForSlot.isEmpty()) {
                                    Icon(
                                        Icons.Default.Add,
                                        null,
                                        modifier = Modifier.size(16.dp).align(Alignment.Center),
                                        tint = MaterialTheme.colorScheme.outlineVariant
                                    )
                                } else {
                                    Column {
                                        plansForSlot.forEach { plan ->
                                            Text(
                                                plan.recipeName,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    .clickable { viewModel.showEditDialog(plan) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }

    // Add/Edit meal dialog
    if (uiState.showAddDialog) {
        MealPlanDialog(
            uiState = uiState,
            isEditing = uiState.editingPlan != null,
            onRecipeNameChange = viewModel::updateDialogRecipeName,
            onNotesChange = viewModel::updateDialogNotes,
            onMealTypeChange = viewModel::updateDialogMealType,
            onIsRoutineChange = viewModel::updateDialogIsRoutine,
            onRoutineTagChange = viewModel::updateDialogRoutineTag,
            onSave = viewModel::saveMealPlan,
            onDelete = {
                uiState.editingPlan?.let { viewModel.deleteMealPlan(it.id) }
                viewModel.hideAddDialog()
            },
            onDismiss = viewModel::hideAddDialog
        )
    }

    // Routine settings dialog
    if (uiState.showRoutineDialog) {
        RoutineDialog(
            routineInput = uiState.routineInput,
            onInputChange = viewModel::updateRoutineInput,
            onDismiss = viewModel::hideRoutineDialog
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealPlanDialog(
    uiState: CalendarUiState,
    isEditing: Boolean,
    onRecipeNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onMealTypeChange: (MealType) -> Unit,
    onIsRoutineChange: (Boolean) -> Unit,
    onRoutineTagChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateLabel = uiState.dialogDate.format(DateTimeFormatter.ofPattern("M月d日"))
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "$dateLabel の献立を編集" else "$dateLabel に献立を追加")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.dialogRecipeName,
                    onValueChange = onRecipeNameChange,
                    label = { Text("料理名 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("食事タイプ", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MEAL_TYPES.forEach { type ->
                        FilterChip(
                            selected = uiState.dialogMealType == type,
                            onClick = { onMealTypeChange(type) },
                            label = { Text(MEAL_LABELS[type] ?: "", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = uiState.dialogNotes,
                    onValueChange = onNotesChange,
                    label = { Text("メモ") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.dialogIsRoutine, onCheckedChange = onIsRoutineChange)
                    Text("毎週のルーティンとして登録", style = MaterialTheme.typography.bodySmall)
                }
                if (uiState.dialogIsRoutine) {
                    OutlinedTextField(
                        value = uiState.dialogRoutineTag,
                        onValueChange = onRoutineTagChange,
                        label = { Text("ルーティンタグ（例: 月曜のヘルシー料理）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isEditing) {
                    TextButton(onClick = onDelete) {
                        Text("削除", color = MaterialTheme.colorScheme.error)
                    }
                }
                Button(onClick = onSave, enabled = uiState.dialogRecipeName.isNotBlank()) {
                    Text(if (isEditing) "更新" else "追加")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}

@Composable
private fun RoutineDialog(
    routineInput: Map<String, String>,
    onInputChange: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val days = listOf("月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "日曜日")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("週間ルーティン設定") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "各曜日の食事テーマを設定できます（例: 月曜日は消化に良いもの）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                days.forEach { day ->
                    OutlinedTextField(
                        value = routineInput[day] ?: "",
                        onValueChange = { onInputChange(day, it) },
                        label = { Text(day) },
                        placeholder = { Text("例: 消化に良いもの") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}
