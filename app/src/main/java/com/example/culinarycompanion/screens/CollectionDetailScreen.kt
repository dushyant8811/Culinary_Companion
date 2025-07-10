package com.example.culinarycompanion.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culinarycompanion.components.RecipeCard
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCollection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CollectionDetailScreen(
    navController: NavController,
    collection: RecipeCollection,
    recipes: List<Recipe>,  // This now contains ONLY the recipes in this collection
    onRecipeClick: (Recipe) -> Unit,
    onRemoveFromCollection: (Recipe) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
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
        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
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
                items(
                    items = recipes,
                    key = { recipe -> recipe.id } // <-- 1. Add key
                ) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement(), // <-- 2. Add animation
                        onClick = { onRecipeClick(recipe) }
                    )
                }
            }
        }
    }
}