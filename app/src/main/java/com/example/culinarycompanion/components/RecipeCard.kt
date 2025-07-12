package com.example.culinarycompanion.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.culinarycompanion.model.Recipe

@Composable
fun RecipeCard(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onFavoriteToggle: (Boolean) -> Unit = {},
    onClick: () -> Unit = {},
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "by ${recipe.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🍽️ Serves ${recipe.servings}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    if (recipe.reviewCount > 0) {
                        Text("•", style = MaterialTheme.typography.labelMedium)
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "%.1f".format(recipe.averageRating) + " (${recipe.reviewCount})",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                IconButton(onClick = { onFavoriteToggle(!recipe.isFavorite) }) {
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                        contentDescription = "Favorite",
                        tint = if (recipe.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (trailingIcon != null) {
                    trailingIcon()
                }
            }
        }
    }
}