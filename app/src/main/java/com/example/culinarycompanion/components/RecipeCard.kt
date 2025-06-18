// components/RecipeCard.kt
package com.example.culinarycompanion.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.culinarycompanion.model.Recipe

@Composable
fun RecipeCard(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onFavoriteToggle: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}  // Add this parameter for click handling
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),  // Make the entire card clickable
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box {
            // Favorite button
            IconButton(
                onClick = { onFavoriteToggle(!recipe.isFavorite) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                    contentDescription = "Favorite",
                    tint = if (recipe.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Category: ${recipe.getCategoryDisplayName()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "⏱️ ${recipe.prepTime} min prep",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "🍳 ${recipe.cookTime} min cook",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🍽️ Serves ${recipe.servings}",
                    style = MaterialTheme.typography.labelMedium
                )

                // Add dietary tags if available
                if (recipe.dietaryTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        recipe.dietaryTags.forEach { tag ->
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}