package com.example.culinarycompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culinarycompanion.components.RecipeCard
import com.example.culinarycompanion.components.SearchBar
import com.example.culinarycompanion.data.DataSource
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCategory
import com.example.culinarycompanion.ui.theme.RecipeBookTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.culinarycompanion.components.RecipeDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipeBookTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "recipeList"
                ) {
                    composable("recipeList") {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            RecipeApp(navController)
                        }
                    }
                    composable("recipeDetail/{recipeId}") { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull()
                        val recipe = DataSource.recipes.find { it.id == recipeId }

                        if (recipe != null) {
                            RecipeDetailScreen(recipe = recipe, navController = navController)
                        } else {
                            Text("Recipe not found", modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeApp(navController: NavController) {
    val allRecipes = remember { DataSource.recipes }
    val searchText = remember { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by remember { mutableStateOf(RecipeCategory.ALL) }
    var selectedDietaryTags by remember { mutableStateOf(emptySet<String>()) }

    Column {
        // Fixed SearchBar call
        SearchBar(
            value = searchText.value,
            onValueChange = { newValue -> searchText.value = newValue }
        )

        // Fixed lambda parameters
        CategoryFilterRow(
            selectedCategory = selectedCategory,
            onCategorySelected = { category -> selectedCategory = category }
        )

        DietaryFilterRow(
            selectedTags = selectedDietaryTags,
            onTagSelected = { tag ->
                selectedDietaryTags = if (selectedDietaryTags.contains(tag)) {
                    selectedDietaryTags - tag
                } else {
                    selectedDietaryTags + tag
                }
            }
        )

        // Added modifier parameter
        RecipeList(
            recipes = allRecipes,
            searchQuery = searchText.value.text,
            selectedCategory = selectedCategory,
            selectedDietaryTags = selectedDietaryTags,
            modifier = Modifier.fillMaxSize(),
            onRecipeClick = { recipe ->
                navController.navigate("recipeDetail/${recipe.id}")
            }
        )
    }
}

// Added modifier parameter with default value
@Composable
fun RecipeList(
    recipes: List<Recipe>,
    searchQuery: String,
    selectedCategory: RecipeCategory,
    selectedDietaryTags: Set<String>,
    modifier: Modifier = Modifier,
    onRecipeClick: (Recipe) -> Unit // Add click handler
) {
    val filteredRecipes = remember(recipes, searchQuery, selectedCategory, selectedDietaryTags) {
        recipes.filter { recipe ->
            val matchesSearch = searchQuery.isBlank() ||
                    recipe.title.contains(searchQuery, ignoreCase = true) ||
                    recipe.ingredients.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesCategory = selectedCategory == RecipeCategory.ALL ||
                    recipe.category == selectedCategory.name

            val matchesDietary = selectedDietaryTags.isEmpty() ||
                    selectedDietaryTags.all { tag -> recipe.dietaryTags.contains(tag) }

            matchesSearch && matchesCategory && matchesDietary
        }
    }

    // FIXED THIS SECTION - removed extra parenthesis
    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filteredRecipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onRecipeClick(recipe) } // Pass click handler
            )
        }
    }
}

@Composable
fun CategoryFilterRow(
    selectedCategory: RecipeCategory,
    onCategorySelected: (RecipeCategory) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .horizontalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        RecipeCategory.values().forEach { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category.toString()) }, // Use toString for display name
                modifier = Modifier.padding(end = 8.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun DietaryFilterRow(
    selectedTags: Set<String>,
    onTagSelected: (String) -> Unit
) {
    val allDietaryTags = remember { setOf("Vegetarian", "Vegan", "Gluten-Free") }
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .horizontalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        allDietaryTags.forEach { tag ->
            FilterChip(
                selected = selectedTags.contains(tag),
                onClick = { onTagSelected(tag) },
                label = { Text(tag) },
                modifier = Modifier.padding(end = 8.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}

// Update RecipeCard to handle clicks
@Composable
fun RecipeCard(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onClick: () -> Unit // Add onClick parameter
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick) // Make card clickable
        // ... rest of existing code ...
    ) {
        // ... existing content ...
    }
}