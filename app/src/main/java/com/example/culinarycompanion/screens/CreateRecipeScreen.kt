package com.example.culinarycompanion.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culinarycompanion.model.RecipeCategory
import com.example.culinarycompanion.viewmodel.AppViewModel
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    navController: NavController,
    viewModel: AppViewModel
) {
    var title by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf("") }
    val instructions = remember { mutableStateListOf("") }
    var prepTime by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(RecipeCategory.DINNER) }
    val selectedTags = remember { mutableStateListOf<String>() }
    val isFormValid = remember(title) { title.isNotBlank() }

    val context = LocalContext.current
    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    val ocrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                try {
                    val image = InputImage.fromFilePath(context, uri)
                    textRecognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            val fullText = visionText.text
                            Log.d("OCR", "Full text found: $fullText")

                            val parsedResult = parseRecipeText(fullText)

                            title = parsedResult.title

                            ingredients.clear()
                            ingredients.addAll(parsedResult.ingredients)
                            if (ingredients.isEmpty()) ingredients.add("")

                            instructions.clear()
                            instructions.addAll(parsedResult.instructions)
                            if (instructions.isEmpty()) instructions.add("")
                        }
                        .addOnFailureListener { e ->
                            Log.e("OCR", "Text recognition failed", e)
                        }
                } catch (e: Exception) {
                    Log.e("OCR", "Failed to load image for OCR", e)
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add a New Recipe") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { ocrLauncher.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scan Recipe with Camera"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isFormValid) {
                        viewModel.createRecipe(
                            title = title,
                            ingredients = ingredients,
                            instructions = instructions,
                            prepTime = prepTime.toIntOrNull() ?: 0,
                            cookTime = cookTime.toIntOrNull() ?: 0,
                            servings = servings.toIntOrNull() ?: 1,
                            category = selectedCategory.name,
                            dietaryTags = selectedTags.toList()
                        )
                        navController.popBackStack()
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Save") },
                text = { Text("Save Recipe") },
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Recipe Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        val categories = RecipeCategory.values().filter { it != RecipeCategory.ALL }
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                SectionTitle("Dietary Tags (Optional)")
                DietaryTagsSelector(
                    selectedTags = selectedTags,
                    onTagSelected = { tag ->
                        if (selectedTags.contains(tag)) {
                            selectedTags.remove(tag)
                        } else {
                            selectedTags.add(tag)
                        }
                    }
                )
            }

            item {
                SectionTitle("Ingredients")
            }
            items(ingredients.size) { index ->
                DynamicTextField(
                    value = ingredients[index],
                    onValueChange = { ingredients[index] = it },
                    onDelete = { ingredients.removeAt(index) },
                    label = "Ingredient ${index + 1}"
                )
            }
            item {
                Button(onClick = { ingredients.add("") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Add Ingredient")
                }
            }

            item {
                SectionTitle("Instructions")
            }
            items(instructions.size) { index ->
                DynamicTextField(
                    value = instructions[index],
                    onValueChange = { instructions[index] = it },
                    onDelete = { instructions.removeAt(index) },
                    label = "Step ${index + 1}"
                )
            }
            item {
                Button(onClick = { instructions.add("") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Add Step")
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prepTime,
                        onValueChange = { prepTime = it },
                        label = { Text("Prep (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cookTime,
                        onValueChange = { cookTime = it },
                        label = { Text("Cook (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = servings,
                        onValueChange = { servings = it },
                        label = { Text("Servings") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// --- Helper Composables ---

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun DynamicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onDelete: () -> Unit,
    label: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DietaryTagsSelector(
    selectedTags: List<String>,
    onTagSelected: (String) -> Unit
) {
    val allTags = listOf(
        "Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free",
        "Nut-Free", "Keto", "Paleo", "Low-Carb"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allTags.forEach { tag ->
            FilterChip(
                selected = selectedTags.contains(tag),
                onClick = { onTagSelected(tag) },
                label = { Text(tag) }
            )
        }
    }
}

data class ParsedRecipe(
    val title: String,
    val ingredients: List<String>,
    val instructions: List<String>
)

fun parseRecipeText(text: String): ParsedRecipe {
    val lines = text.lines().filter { it.isNotBlank() }

    var title = lines.firstOrNull() ?: ""

    val ingredients = mutableListOf<String>()
    val instructions = mutableListOf<String>()

    var isIngredientsSection = false
    var isInstructionsSection = false

    for (line in lines.drop(1)) {
        val lowerLine = line.trim().lowercase()

        if (lowerLine.contains("ingredient")) {
            isIngredientsSection = true
            isInstructionsSection = false
            continue
        }
        if (lowerLine.contains("instruction") || lowerLine.contains("direction") || lowerLine.contains("method")) {
            isInstructionsSection = true
            isIngredientsSection = false
            continue
        }

        if (isIngredientsSection) {
            ingredients.add(line.trim())
        } else if (isInstructionsSection) {
            instructions.add(line.trim())
        }
    }

    if (ingredients.isEmpty() && instructions.isEmpty()) {
        val splitPoint = (lines.size * 0.25).toInt()
        ingredients.addAll(lines.drop(1).take(splitPoint))
        instructions.addAll(lines.drop(1 + splitPoint))
    }

    return ParsedRecipe(title, ingredients, instructions)
}