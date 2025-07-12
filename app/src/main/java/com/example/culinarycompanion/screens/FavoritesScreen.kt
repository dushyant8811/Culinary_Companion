package com.example.culinarycompanion.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culinarycompanion.components.RecipeCard
import com.example.culinarycompanion.model.Recipe

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    recipes: List<Recipe>,
    onFavoriteToggle: (Recipe, Boolean) -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    val favoriteRecipes = remember(recipes) {
        recipes.filter { it.isFavorite }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Recipes") },
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
        if (favoriteRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "No favorites",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No favorite recipes yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Tap the heart icon on recipes to add them here",
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
                    items = favoriteRecipes,
                    key = { recipe -> recipe.id }
                ) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement(),
                        onFavoriteToggle = { isFavorite ->
                            onFavoriteToggle(recipe, isFavorite)
                        },
                        onClick = {
                            onRecipeClick(recipe)
                        }
                    )
                }
            }
        }
    }
}