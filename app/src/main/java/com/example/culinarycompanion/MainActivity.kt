package com.example.culinarycompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.culinarycompanion.components.*
import com.example.culinarycompanion.data.DataSource
import com.example.culinarycompanion.database.AppDatabase
import com.example.culinarycompanion.database.CollectionDao
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCategory
import com.example.culinarycompanion.model.RecipeCollection
import com.example.culinarycompanion.repository.CollectionRepository
import com.example.culinarycompanion.screens.*
import com.example.culinarycompanion.ui.theme.RecipeBookTheme
import com.example.culinarycompanion.viewmodel.AppViewModel
import com.example.culinarycompanion.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and repository
        val appDatabase = AppDatabase.getDatabase(applicationContext)
        val collectionDao = appDatabase.collectionDao()
        val collectionRepository = CollectionRepository(collectionDao)

        // Create ViewModel factory
        val viewModelFactory = AppViewModelFactory(collectionRepository)
        val viewModel: AppViewModel by viewModels { viewModelFactory }

        // Add this to MainActivity.kt
        @Composable
        fun AddCollectionDialog(
            onDismiss: () -> Unit,
            onConfirm: (String) -> Unit
        ) {
            var name by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Create New Collection") },
                text = {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Collection Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { onConfirm(name) },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }

        setContent {
            RecipeBookTheme {
                val navController = rememberNavController()
                val collections by viewModel.collections.collectAsState()
                val allRecipes = remember { DataSource.recipes }

                NavHost(
                    navController = navController,
                    startDestination = "recipeList"
                ) {
                    composable("recipeList") {
                        RecipeApp(
                            navController = navController,
                            recipes = allRecipes,
                            collections = collections,
                            onFavoriteToggle = { recipe, isFavorite ->
                                viewModel.toggleFavorite(recipe, isFavorite)
                            },
                            onAddToCollection = { recipe, collectionId ->
                                viewModel.addToCollection(recipe, collectionId)
                            }
                        )
                    }
                    composable("recipeDetail/{recipeId}") { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull()
                        val recipe = allRecipes.find { it.id == recipeId }

                        if (recipe != null) {
                            RecipeDetailScreen(
                                recipe = recipe,
                                navController = navController,
                                collections = collections,
                                onFavoriteToggle = { isFavorite ->
                                    viewModel.toggleFavorite(recipe, isFavorite)
                                },
                                onAddToCollection = { collectionId ->
                                    viewModel.addToCollection(recipe, collectionId)
                                }
                            )
                        } else {
                            Text("Recipe not found", modifier = Modifier.fillMaxSize())
                        }
                    }
                    composable("favorites") {
                        FavoritesScreen(
                            navController = navController,
                            recipes = allRecipes,
                            onFavoriteToggle = { recipe, isFavorite ->
                                viewModel.toggleFavorite(recipe, isFavorite)
                            },
                            onRecipeClick = { recipe ->
                                navController.navigate("recipeDetail/${recipe.id}")
                            }
                        )
                    }
                    composable("collections") {
                        CollectionsScreen(
                            navController = navController,
                            collections = collections,
                            recipes = allRecipes,
                            onCollectionClick = { collectionId ->
                                navController.navigate("collectionDetail/$collectionId")
                            },
                            onCreateCollection = {
                                navController.navigate("createCollection")
                            },
                            onDeleteCollection = { collection ->
                                viewModel.deleteCollection(collection)
                            }
                        )
                    }
                    composable("collectionDetail/{collectionId}") { backStackEntry ->
                        val collectionId = backStackEntry.arguments?.getString("collectionId")?.toLongOrNull()
                        val collection = collections.find { it.id == collectionId }

                        if (collection != null) {
                            CollectionDetailScreen(
                                navController = navController,
                                collection = collection,
                                recipes = allRecipes,
                                onRecipeClick = { recipe ->
                                    navController.navigate("recipeDetail/${recipe.id}")
                                },
                                onRemoveFromCollection = { recipe ->
                                    viewModel.removeFromCollection(recipe, collection.id)
                                }
                            )
                        } else {
                            Text("Collection not found", modifier = Modifier.fillMaxSize())
                        }
                    }
                    composable("createCollection") {
                        var showDialog by remember { mutableStateOf(true) }

                        if (showDialog) {
                            AddCollectionDialog(
                                onDismiss = {
                                    showDialog = false
                                    navController.popBackStack()
                                },
                                onConfirm = { name ->
                                    viewModel.createCollection(name)
                                    showDialog = false
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeApp(
    navController: NavController,
    recipes: List<Recipe>,
    collections: List<RecipeCollection>,
    onFavoriteToggle: (Recipe, Boolean) -> Unit,
    onAddToCollection: (Recipe, Long) -> Unit
) {
    val searchText = remember { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by remember { mutableStateOf(RecipeCategory.ALL) }
    var selectedDietaryTags by remember { mutableStateOf(emptySet<String>()) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Culinary Companion") },
                actions = {
                    IconButton(onClick = { navController.navigate("favorites") }) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favorites")
                    }
                    IconButton(onClick = { navController.navigate("collections") }) {
                        Icon(Icons.Filled.Folder, contentDescription = "Collections")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            SearchBar(
                value = searchText.value,
                onValueChange = { newValue -> searchText.value = newValue }
            )

            // Fixed: Category filter row implementation
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { category -> selectedCategory = category }
            )

            // Fixed: Dietary filter row implementation
            DietaryFilterRow(
                selectedTags = selectedDietaryTags,
                onTagSelected = { tag ->
                    selectedDietaryTags = if (selectedDietaryTags.contains(tag)) {
                        selectedDietaryTags.minusElement(tag)  // Fixed ambiguity
                    } else {
                        selectedDietaryTags.plusElement(tag)   // Fixed ambiguity
                    }
                }
            )

            RecipeList(
                recipes = recipes,
                searchQuery = searchText.value.text,
                selectedCategory = selectedCategory,
                selectedDietaryTags = selectedDietaryTags,
                modifier = Modifier.fillMaxSize(),
                onRecipeClick = { recipe ->
                    navController.navigate("recipeDetail/${recipe.id}")
                },
                onFavoriteToggle = onFavoriteToggle
            )
        }
    }
}

@Composable
fun RecipeList(
    recipes: List<Recipe>,
    searchQuery: String,
    selectedCategory: RecipeCategory,
    selectedDietaryTags: Set<String>,
    modifier: Modifier = Modifier,
    onRecipeClick: (Recipe) -> Unit,
    onFavoriteToggle: (Recipe, Boolean) -> Unit
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

    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filteredRecipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                modifier = Modifier.fillMaxWidth(),
                onFavoriteToggle = { isFavorite -> onFavoriteToggle(recipe, isFavorite) },
                onClick = { onRecipeClick(recipe) }
            )
        }
    }
}

// Add these missing composable functions
@Composable
fun CategoryFilterRow(
    selectedCategory: RecipeCategory,
    onCategorySelected: (RecipeCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .padding(vertical = 8.dp)
            .horizontalScroll(scrollState)
    ) {
        RecipeCategory.values().forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                modifier = Modifier.padding(horizontal = 4.dp),
                label = { Text(category.name) }
            )
        }
    }
}

@Composable
fun DietaryFilterRow(
    selectedTags: Set<String>,
    onTagSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dietaryTags = listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free", "Nut-Free", "Keto", "Paleo")
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .padding(vertical = 8.dp)
            .horizontalScroll(scrollState)
    ) {
        dietaryTags.forEach { tag ->
            FilterChip(
                selected = selectedTags.contains(tag),
                onClick = { onTagSelected(tag) },
                modifier = Modifier.padding(horizontal = 4.dp),
                label = { Text(tag) }
            )
        }
    }
}