package com.example.culinarycompanion

import PreferencesManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModelProvider
import com.example.culinarycompanion.components.*
import com.example.culinarycompanion.data.DataSource
import com.example.culinarycompanion.database.AppDatabase
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCategory
import com.example.culinarycompanion.model.RecipeCollection
import com.example.culinarycompanion.repository.CollectionRepository
import com.example.culinarycompanion.screens.*
import com.example.culinarycompanion.ui.theme.RecipeBookTheme
import com.example.culinarycompanion.viewmodel.AppViewModel
import com.example.culinarycompanion.viewmodel.AppViewModelFactory
import com.example.culinarycompanion.viewmodel.AuthViewModel
import com.example.culinarycompanion.repository.RecipeRepository
import com.example.culinarycompanion.repository.FirebaseRecipeRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var appViewModel: AppViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)

        // Initialize Firebase Auth
        Firebase.auth.useAppLanguage()
        authViewModel.initialize(this)

        // Use Firebase repository directly
        val firebaseRepo = FirebaseRecipeRepository()

        val recipeRepository = object : RecipeRepository {
            override suspend fun getAllRecipes(): List<Recipe> {
                return firebaseRepo.getAllRecipes()
            }

            override suspend fun getRecipeById(id: String): Recipe? {
                return firebaseRepo.getRecipeById(id)
            }

        }

        // Initialize database and repository
        val appDatabase = AppDatabase.getDatabase(applicationContext)
        val collectionDao = appDatabase.collectionDao()
        val savedRecipeDao = appDatabase.savedRecipeDao()

        val collectionRepository = CollectionRepository(collectionDao, savedRecipeDao)
        val viewModelFactory = AppViewModelFactory(collectionRepository, recipeRepository)

        appViewModel = ViewModelProvider(this, viewModelFactory)[AppViewModel::class.java]

        authViewModel.checkAuthState()

        setContent {
            RecipeBookTheme {
                val navController = rememberNavController()
                val currentUser by authViewModel.currentUser.collectAsState()
                val isLoading by authViewModel.isLoading.collectAsState()

                // Handle auth state with navigation
                LaunchedEffect(currentUser, isLoading) {
                    when {
                        isLoading -> {
                            if (navController.currentDestination?.route != "loading") {
                                navController.navigate("loading") {
                                    launchSingleTop = true
                                }
                            }
                        }

                        currentUser == null -> {
                            navController.navigate("login") {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        }

                        !preferencesManager.isProfileSetupComplete -> {
                            navController.navigate("profileSetup") {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        }

                        else -> {
                            navController.navigate("recipeList") {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "loading"
                ) {
                    composable("loading") {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    composable("login") {
                        LoginScreen(navController, authViewModel)
                    }

                    composable("profileSetup") {
                        ProfileSetupScreen(
                            navController = navController,
                            authViewModel = authViewModel,
                            onProfileComplete = {
                                navController.navigate("recipeList") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("recipeList") {
                        val collections by appViewModel.collections.collectAsState(emptyList())
                        val recipes by appViewModel.recipes.collectAsState(emptyList())

                        RecipeApp(
                            navController = navController,
                            recipes = recipes,
                            collections = collections,
                            onFavoriteToggle = { recipe, isFavorite ->
                                appViewModel.toggleFavorite(recipe, isFavorite)
                            },
                            onAddToCollection = { recipe, collectionId ->
                                appViewModel.addToCollection(recipe, collectionId)
                            }
                        )
                    }

                    composable("recipeDetail/{recipeId}") { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId")
                        val recipes by appViewModel.recipes.collectAsState(emptyList())
                        val recipe = recipes.find { it.id == recipeId }

                        if (recipe != null) {
                            val collections by appViewModel.collections.collectAsState(emptyList())

                            RecipeDetailScreen(
                                recipe = recipe,
                                navController = navController,
                                collections = collections,
                                onFavoriteToggle = { isFavorite ->
                                    appViewModel.toggleFavorite(recipe, isFavorite)
                                },
                                onAddToCollection = { collectionId ->
                                    appViewModel.addToCollection(recipe, collectionId)
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Recipe not found")
                            }
                        }
                    }

                    composable("favorites") {
                        val recipes by appViewModel.recipes.collectAsState(emptyList())
                        val favoriteRecipes = remember(recipes) {
                            recipes.filter { it.isFavorite }
                        }

                        FavoritesScreen(
                            navController = navController,
                            recipes = favoriteRecipes,
                            onFavoriteToggle = { recipe, isFavorite ->
                                appViewModel.toggleFavorite(recipe, isFavorite)
                            },
                            onRecipeClick = { recipe ->
                                navController.navigate("recipeDetail/${recipe.id}") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("collections") {
                        val collections by appViewModel.collections.collectAsState(emptyList())

                        CollectionsScreen(
                            navController = navController,
                            collections = collections,
                            onCollectionClick = { collectionId ->
                                navController.navigate("collectionDetail/$collectionId") {
                                    launchSingleTop = true
                                }
                            },
                            onCreateCollection = {
                                navController.navigate("createCollection") {
                                    launchSingleTop = true
                                }
                            },
                            onDeleteCollection = { collection ->
                                appViewModel.deleteCollection(collection)
                            }
                        )
                    }

                    composable("collectionDetail/{collectionId}") { backStackEntry ->
                        val collectionId = backStackEntry.arguments?.getString("collectionId")?.toLongOrNull()
                        val collections by appViewModel.collections.collectAsState(emptyList())
                        val collection = collections.find { it.id == collectionId }

                        if (collection != null) {
                            val recipes by appViewModel.recipes.collectAsState(emptyList())
                            val collectionRecipes = remember(recipes, collection) {
                                recipes.filter { recipe -> recipe.id in collection.recipeIds }
                            }

                            CollectionDetailScreen(
                                navController = navController,
                                collection = collection,
                                recipes = collectionRecipes,
                                onRecipeClick = { recipe ->
                                    navController.navigate("recipeDetail/${recipe.id}")
                                },
                                onRemoveFromCollection = { recipe ->
                                    appViewModel.removeFromCollection(recipe, collection.id)
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Collection not found")
                            }
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
                                    appViewModel.createCollection(name)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthViewModel.RC_SIGN_IN) {
            authViewModel.handleSignInResult(data)
        }
    }

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
                        IconButton(
                            onClick = {
                                navController.navigate("favorites") {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.testTag("favorites_button")
                        ) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Favorites")
                        }
                        IconButton(
                            onClick = {
                                navController.navigate("collections") {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.testTag("collections_button")
                        ) {
                            Icon(Icons.Filled.Folder, contentDescription = "Collections")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                SearchBar(
                    value = searchText.value,
                    onValueChange = { newValue -> searchText.value = newValue }
                )

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

                RecipeList(
                    recipes = recipes,
                    searchQuery = searchText.value.text,
                    selectedCategory = selectedCategory,
                    selectedDietaryTags = selectedDietaryTags,
                    modifier = Modifier.fillMaxSize(),
                    onRecipeClick = { recipe ->
                        navController.navigate("recipeDetail/${recipe.id}") {
                            launchSingleTop = true
                        }
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
        val filteredRecipes =
            remember(recipes, searchQuery, selectedCategory, selectedDietaryTags) {
                if (recipes.isEmpty()) {
                    emptyList()
                } else {
                    recipes.filter { recipe ->
                        val matchesSearch = searchQuery.isBlank() ||
                                recipe.title.contains(searchQuery, ignoreCase = true) ||
                                recipe.ingredients.any {
                                    it.contains(
                                        searchQuery,
                                        ignoreCase = true
                                    )
                                }

                        val matchesCategory = selectedCategory == RecipeCategory.ALL ||
                                recipe.category == selectedCategory.name

                        val matchesDietary = selectedDietaryTags.isEmpty() ||
                                selectedDietaryTags.all { tag -> recipe.dietaryTags.contains(tag) }

                        matchesSearch && matchesCategory && matchesDietary
                    }
                }
            }

        if (filteredRecipes.isEmpty()) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No recipes found")
            }
        } else {
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
    }

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
        val dietaryTags =
            listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free", "Nut-Free", "Keto", "Paleo")
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
}