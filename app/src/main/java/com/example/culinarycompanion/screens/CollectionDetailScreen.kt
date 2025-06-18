package com.example.culinarycompanion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder // Direct import
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culinarycompanion.components.RecipeCard
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCollection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    navController: NavController,
    collection: RecipeCollection,
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onRemoveFromCollection: (Recipe) -> Unit
) {
    val collectionRecipes = remember(collection, recipes) {
        recipes.filter { it.id in collection.recipeIds }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(collection.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (collectionRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Folder, // Using directly imported icon
                        contentDescription = "Empty collection",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "This collection is empty",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Add recipes from their detail screens",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(collectionRecipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onRecipeClick(recipe) }
                    )
                }
            }
        }
    }
}