package com.americanpizzabar.food.presentation.screen.photoanalysis

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.americanpizzabar.food.domain.model.Ingredient
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PhotoAnalysisScreen(
    onNavigateBack: () -> Unit,
    viewModel: PhotoAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            viewModel.analyzePhoto(bitmap)
        }
    }

    val context = LocalContext.current
    val galleryUriLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                @Suppress("DEPRECATION")
                val bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                    context.contentResolver, it
                )
                capturedBitmap = bitmap
                viewModel.analyzePhoto(bitmap)
            } catch (e: Exception) {
                // error surfaced via ViewModel error state
            }
        }
    }

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

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("レシピを保存しました")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("写真解析") },
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
            // Camera / Image picker section
            item {
                CameraSection(
                    capturedBitmap = capturedBitmap,
                    isAnalyzing = uiState.isAnalyzing,
                    hasCameraPermission = cameraPermissionState.status.isGranted,
                    showPermissionRationale = cameraPermissionState.status.shouldShowRationale,
                    onRequestCameraPermission = { cameraPermissionState.launchPermissionRequest() },
                    onOpenCamera = {
                        if (cameraPermissionState.status.isGranted) {
                            cameraLauncher.launch()
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                    onOpenGallery = { galleryUriLauncher.launch("image/*") }
                )
            }

            // Analysis results
            val recipe = uiState.editedRecipe
            if (recipe != null && !uiState.isAnalyzing) {

                // Basic info
                item {
                    RecipeBasicInfoSection(
                        recipeName = recipe.name,
                        description = recipe.description,
                        calories = recipe.calories,
                        onNameChange = viewModel::updateRecipeName,
                        onDescriptionChange = viewModel::updateDescription,
                        onCaloriesChange = viewModel::updateCalories
                    )
                }

                // Nutrition
                item {
                    NutritionInfoCard(recipe = recipe)
                }

                // Ingredients
                item {
                    IngredientsSection(
                        ingredients = recipe.ingredients,
                        onIngredientUpdate = viewModel::updateIngredient,
                        onIngredientAdd = viewModel::addIngredient,
                        onIngredientRemove = viewModel::removeIngredient
                    )
                }

                // Steps
                item {
                    StepsSection(
                        steps = recipe.steps,
                        onStepUpdate = viewModel::updateStep,
                        onStepAdd = viewModel::addStep,
                        onStepRemove = viewModel::removeStep
                    )
                }

                // Plating advice
                if (recipe.platingAdvice?.isNotEmpty() == true) {
                    item {
                        PlatingAdviceCard(advice = recipe.platingAdvice)
                    }
                }

                // Remake ideas
                if (recipe.remakeIdeas?.isNotEmpty() == true) {
                    item {
                        RemakeIdeasCard(ideas = recipe.remakeIdeas)
                    }
                }

                // Action buttons
                item {
                    ActionButtonsSection(
                        isSaved = uiState.isSaved,
                        onSave = viewModel::saveRecipe,
                        onAddToShoppingList = viewModel::generateShoppingList
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Camera / picker section
// ---------------------------------------------------------------------------

@Composable
private fun CameraSection(
    capturedBitmap: Bitmap?,
    isAnalyzing: Boolean,
    hasCameraPermission: Boolean,
    showPermissionRationale: Boolean,
    onRequestCameraPermission: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (capturedBitmap != null) {
                Image(
                    bitmap = capturedBitmap.asImageBitmap(),
                    contentDescription = "撮影した料理",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "料理の写真を撮影または選択",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (isAnalyzing) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("AIが解析中...", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
            } else {
                if (!hasCameraPermission && showPermissionRationale) {
                    Text(
                        "カメラを使用するには権限が必要です",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRequestCameraPermission) {
                        Text("カメラ権限を許可")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenCamera,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("カメラ")
                        }
                        OutlinedButton(
                            onClick = onOpenGallery,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("ギャラリー")
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Basic recipe info
// ---------------------------------------------------------------------------

@Composable
private fun RecipeBasicInfoSection(
    recipeName: String,
    description: String,
    calories: Int,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "レシピ情報",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = recipeName,
                onValueChange = onNameChange,
                label = { Text("料理名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("説明") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            OutlinedTextField(
                value = if (calories > 0) calories.toString() else "",
                onValueChange = onCaloriesChange,
                label = { Text("カロリー (kcal)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = { Text("kcal", style = MaterialTheme.typography.bodySmall) }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Nutrition info card
// ---------------------------------------------------------------------------

@Composable
private fun NutritionInfoCard(recipe: com.americanpizzabar.food.domain.model.Recipe) {
    val nutrition = recipe.nutrition ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "栄養情報",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionItem(label = "たんぱく質", value = "${nutrition.protein}g")
                NutritionItem(label = "炭水化物", value = "${nutrition.carbs}g")
                NutritionItem(label = "脂質", value = "${nutrition.fat}g")
            }
        }
    }
}

@Composable
private fun NutritionItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

// ---------------------------------------------------------------------------
// Ingredients section
// ---------------------------------------------------------------------------

@Composable
private fun IngredientsSection(
    ingredients: List<Ingredient>,
    onIngredientUpdate: (Int, Ingredient) -> Unit,
    onIngredientAdd: (Ingredient) -> Unit,
    onIngredientRemove: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "材料 (${ingredients.size}品)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "材料を追加")
                }
            }

            ingredients.forEachIndexed { index, ingredient ->
                IngredientRow(
                    ingredient = ingredient,
                    onUpdate = { updated -> onIngredientUpdate(index, updated) },
                    onRemove = { onIngredientRemove(index) }
                )
                if (index < ingredients.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddIngredientDialog(
            onConfirm = { ingredient ->
                onIngredientAdd(ingredient)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun IngredientRow(
    ingredient: Ingredient,
    onUpdate: (Ingredient) -> Unit,
    onRemove: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var nameText by remember(ingredient.name) { mutableStateOf(ingredient.name) }
    var amountText by remember(ingredient.amount) { mutableStateOf(ingredient.amount) }

    if (isEditing) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("名前") },
                modifier = Modifier.weight(1.5f),
                singleLine = true
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("量") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = {
                onUpdate(ingredient.copy(name = nameText, amount = amountText))
                isEditing = false
            }) {
                Icon(Icons.Default.Check, "確定", tint = MaterialTheme.colorScheme.primary)
            }
        }
    } else {
        ListItem(
            headlineContent = { Text(ingredient.name) },
            supportingContent = { Text(ingredient.amount) },
            trailingContent = {
                Row {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Edit, "編集", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onRemove) {
                        Icon(
                            Icons.Default.Delete, "削除",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun AddIngredientDialog(
    onConfirm: (Ingredient) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("材料を追加") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("材料名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("量 (例: 200g)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(Ingredient(name = name, amount = amount))
                    }
                }
            ) { Text("追加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}

// ---------------------------------------------------------------------------
// Steps section
// ---------------------------------------------------------------------------

@Composable
private fun StepsSection(
    steps: List<String>,
    onStepUpdate: (Int, String) -> Unit,
    onStepAdd: (String) -> Unit,
    onStepRemove: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "作り方 (${steps.size}ステップ)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "ステップを追加")
                }
            }

            steps.forEachIndexed { index, step ->
                StepRow(
                    stepNumber = index + 1,
                    step = step,
                    onUpdate = { updated -> onStepUpdate(index, updated) },
                    onRemove = { onStepRemove(index) }
                )
                if (index < steps.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddStepDialog(
            stepNumber = steps.size + 1,
            onConfirm = { step ->
                onStepAdd(step)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun StepRow(
    stepNumber: Int,
    step: String,
    onUpdate: (String) -> Unit,
    onRemove: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var text by remember(step) { mutableStateOf(step) }

    if (isEditing) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("ステップ $stepNumber") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { isEditing = false }) { Text("キャンセル") }
                TextButton(onClick = {
                    onUpdate(text)
                    isEditing = false
                }) { Text("確定") }
            }
        }
    } else {
        ListItem(
            headlineContent = {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "$stepNumber",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Text(step, style = MaterialTheme.typography.bodyMedium)
                }
            },
            trailingContent = {
                Row {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Edit, "編集", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onRemove) {
                        Icon(
                            Icons.Default.Delete, "削除",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun AddStepDialog(
    stepNumber: Int,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ステップ $stepNumber を追加") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("手順を入力") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text) }
            ) { Text("追加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}

// ---------------------------------------------------------------------------
// Plating advice card
// ---------------------------------------------------------------------------

@Composable
private fun PlatingAdviceCard(advice: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    "盛り付けアドバイス",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                advice,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Remake ideas card
// ---------------------------------------------------------------------------

@Composable
private fun RemakeIdeasCard(ideas: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "アレンジアイデア",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            ideas.forEach { idea ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text("•", style = MaterialTheme.typography.bodyMedium)
                    Text(idea, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Action buttons
// ---------------------------------------------------------------------------

@Composable
private fun ActionButtonsSection(
    isSaved: Boolean,
    onSave: () -> Unit,
    onAddToShoppingList: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaved
        ) {
            Icon(
                if (isSaved) Icons.Default.Check else Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isSaved) "保存済み" else "レシピを保存")
        }
        OutlinedButton(
            onClick = onAddToShoppingList,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("買い物リストに追加")
        }
    }
}
