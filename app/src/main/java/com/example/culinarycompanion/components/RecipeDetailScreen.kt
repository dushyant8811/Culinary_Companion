// components/RecipeDetailScreen.kt
package com.example.culinarycompanion.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCollection
import com.example.culinarycompanion.screens.CookingModeScreen
import com.example.culinarycompanion.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    viewModel: AppViewModel,
    navController: NavController,
    collections: List<RecipeCollection>,
    isDownloaded: Boolean,
    onFavoriteToggle: (Boolean) -> Unit = {},
    onAddToCollection: (String) -> Unit = {},
    onDownloadClick: () -> Unit
) {
    var showCollectionsDialog by remember { mutableStateOf(false) }
    val isReadingAloud by viewModel.isReadingAloud.collectAsState()

    if (isReadingAloud) {
        CookingModeScreen(
            viewModel = viewModel,
            onStop = { viewModel.stopReading() }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(recipe.title) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {

                        IconButton(onClick = onDownloadClick) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.FileDownload,
                                contentDescription = "Download Recipe",
                                tint = if (recipe.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { onFavoriteToggle(!recipe.isFavorite) }) {
                            Icon(
                                imageVector = if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                                contentDescription = "Favorite",
                                tint = if (recipe.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { showCollectionsDialog = true },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Add to Collection") },
                        text = { Text("Add to Collection") }
                    )

                    ExtendedFloatingActionButton(
                        onClick = { viewModel.startReading(recipe) },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Start Cooking") },
                        text = { Text("Start Cooking") }
                    )
                }
            }

        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Recipe Image
                recipe.imageUrl?.let { url ->
                    Image(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = "Recipe Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "By ${recipe.author}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Basic Information
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChip(label = "Prep", value = "${recipe.prepTime} min")
                    InfoChip(label = "Cook", value = "${recipe.cookTime} min")
                    InfoChip(label = "Serves", value = recipe.servings.toString())
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ingredients Section
                SectionTitle("Ingredients")
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    recipe.ingredients.forEachIndexed { index, ingredient ->
                        Text(
                            text = "• $ingredient",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Instructions Section
                SectionTitle("Instructions")
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    recipe.instructions.forEachIndexed { index, step ->
                        Text(
                            text = "${index + 1}. $step",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Collections Dialog
            if (showCollectionsDialog) {
                AlertDialog(
                    onDismissRequest = { showCollectionsDialog = false },
                    title = { Text("Add to Collection") },
                    text = {
                        LazyColumn {
                            if (collections.isEmpty()) {
                                item {
                                    Text("No collections yet. Create one first.")
                                }
                            } else {
                                items(collections) { collection ->
                                    val containsRecipe = recipe.id in collection.recipeIds
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (!containsRecipe) {
                                                    onAddToCollection(collection.id)
                                                }
                                                showCollectionsDialog = false
                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (containsRecipe) Icons.Default.Check else Icons.Default.Folder,
                                            contentDescription = "Collection",
                                            tint = if (containsRecipe) Color.Green else MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = collection.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            navController.navigate("collections")
                            showCollectionsDialog = false
                        }) {
                            Text("Manage Collections")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCollectionsDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}