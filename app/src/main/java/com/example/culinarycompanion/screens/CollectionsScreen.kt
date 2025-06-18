package com.example.culinarycompanion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culinarycompanion.components.CollectionItem
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCollection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    navController: NavController,
    collections: List<RecipeCollection>,
    recipes: List<Recipe>,
    onCollectionClick: (Long) -> Unit,
    onDeleteCollection: (RecipeCollection) -> Unit,
    onCreateCollection: () -> Unit
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("My Collections") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateCollection,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Collection") },
                text = { Text("New Collection") }
            )
        }
    ) { innerPadding ->
        if (collections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = "No collections",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No recipe collections yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Create your first collection to organize recipes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onCreateCollection) {
                        Text("Create Collection")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(collections) { collection ->
                    val recipeCount = recipes.count { it.id in collection.recipeIds }
                    CollectionItem(
                        collection = collection,
                        recipeCount = recipeCount,
                        onCollectionClick = { onCollectionClick(collection.id) },
                        onDeleteClick = { onDeleteCollection(collection) }
                    )
                }
            }
        }
    }
}